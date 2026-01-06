package org.rj.modelgen.bpmn.models.generation.validation;

import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.*;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType;
import org.rj.modelgen.bpmn.intrep.model.*;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.*;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.*;
import static org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType.*;
import static org.rj.modelgen.bpmn.models.generation.validation.BpmnScriptUtils.*;
import static org.rj.modelgen.llm.util.ValidationUtils.identifyNumberOfRoots;
import static org.rj.modelgen.llm.util.ValidationUtils.convertStringToMap;

public class ValidateBpmnModel {

    private static final String FULL_PROCESS = "full_process";
    private static final String COMMA_DELIMITER = ",";

    private BpmnIntermediateModel model;
    private BpmnGlobalVariableLibrary globalVariableLibrary;
    private BpmnComponentLibrary componentLibrary;
    private List<IntermediateModelValidationError> invalidMessages = new ArrayList<>();
    private final GroovyShell shell = new GroovyShell();

    public ValidateBpmnModel(BpmnIntermediateModel model, BpmnGlobalVariableLibrary globalVariableLibrary, BpmnComponentLibrary componentLibrary) {
        this.model = model;
        this.globalVariableLibrary = globalVariableLibrary;
        this.componentLibrary = componentLibrary;
    }

    public List<IntermediateModelValidationError> validate(Map<String, Set<PayloadVariable>> startingPayload) {
        for (ElementNode node : model.getNodes()) {
            validateNodeNames(node);
            validateRequiredInputs(node);
            validateNodeConnections(node);
            validateNodeConnectionsRules(node);
        }
        identifyOrphanedNodes();

        // Code validation requires traversing a valid graph in execution order so execute it after successful graph structure validation
        if (invalidMessages.isEmpty()) {
            List<Consumer<ProcessState>> validationFunctions = List.of(
                    this::validateCodeGenInputs
            );
            traverseGraphInExecutionOrder(validationFunctions, startingPayload);
        }

        return invalidMessages;
    }

    private void validateNodeNames(ElementNode node) {
        if (StringUtils.isBlank(node.getName())) {
            invalidMessages.add(new IntermediateModelValidationError("Node name cannot be null or blank", FULL_PROCESS));
        }
        if (StringUtils.isBlank(node.getId())) {
            invalidMessages.add(new IntermediateModelValidationError("Node id cannot be null or blank", FULL_PROCESS));
        } else {
            if (StringUtils.isBlank(node.getElementType())) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node type cannot be null or blank for node '%s' ", node.getId()), node.getId()));
            }
            var component = componentLibrary.getComponentByName(node.getElementType());
            if (component.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Component '%s' is not found in library for node %s. Select a valid component.", node.getElementType(), node.getId()), node.getId()));
            }
        }
    }

    private void validateRequiredInputs(ElementNode node) {
        final Collection<IntermediateModelValidationError> invalidInputMessages = new ArrayList<>();
        final var component = componentLibrary.getComponentByName(node.getElementType());
        if (component.isEmpty()) return;   // Unknown action type, should probably never happen by the time we reach this point
        if (node.getInputs() == null) return;

        node.getInputs().forEach(nodeInput -> {
            if (StringUtils.isBlank(nodeInput.getName())) {
                invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has input with null or empty name", node.getId()), node.getId()));
            } else {
                if (StringUtils.isBlank(nodeInput.getValue())) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null or empty value", node.getId(), nodeInput.getName()), node.getId()));
                }
                if (StringUtils.isBlank(nodeInput.getVariableSource())) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null or empty variableSource. It should be CONSTANT, EXPRESSION, SCRIPT, NODE or GLOBAL.", node.getId(), nodeInput.getName()), node.getId()));
                }
            }
        });

        if (!invalidInputMessages.isEmpty()) {
            // All inputs must have valid values before proceeding with further validation
            invalidMessages.addAll(invalidInputMessages);
            return;
        }

        component.get().getRequiredInputs().forEach(requiredInput -> {
            List<ElementNodeInput> inputs = node.getInputs() == null
                ? new ArrayList<>()
                : node.getInputs().stream()
                    .filter(nodeInput -> nodeInput.getName().equals(requiredInput.getName()))
                    .toList();

            // If the input is mandatory, it must be present
            if (requiredInput.isMandatory() && inputs.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' is missing mandatory input '%s'", node.getId(), requiredInput.getName()), node.getId()));
            }
            // Input must not be defined multiple times
            if (inputs.size() > 1) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has multiple definitions of input '%s'", node.getId(), requiredInput.getName()), node.getId()));
            }
            // If the input is enum and constant, it must have a valid value
            if (requiredInput.getAllowedValues() != null && !requiredInput.getAllowedValues().isEmpty() && inputs.get(0).getVariableSource().equals(CONSTANT.toString()) && !requiredInput.getAllowedValues().contains(inputs.get(0).getValue())) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an invalid value for input '%s'. Allowed values are: %s", node.getId(), requiredInput.getName(), String.join(", ", requiredInput.getAllowedValues())), node.getId()));
            }
            // If the input's source type is GLOBAL, then global variable library must contain the input variable
            if (!inputs.isEmpty() && inputs.get(0).getVariableSource().equals(GLOBAL.toString()) && globalVariableLibrary.getVariableByName(inputs.get(0).getValue()).isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an input '%s' which tries to use a global variable '%s' that does not exist in the global variable library",
                        node.getId(), requiredInput.getName(), inputs.get(0).getValue()), node.getId()));
            }
            // Input's source type must be among the component allowed source types
            if (!inputs.isEmpty() && !requiredInput.isAllowedInputSourceType(inputs.get(0).getVariableSource())) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an input '%s' with invalid variable source type '%s'. Allowed source types for this input are: %s",
                        node.getId(), requiredInput.getName(), inputs.get(0).getVariableSource(), String.join(", ", requiredInput.getAllowedInputSourceTypes().stream().map(BpmnComponentInputSourceType::toString).toList())), node.getId()));
            }
        });
    }

    private void validateNodeConnections(ElementNode node) {
        if (node.getConnectedTo() != null) {
            node.getConnectedTo().forEach(connection -> {
                if (StringUtils.isBlank(connection.getTargetNode())) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Connection for node '%s' has no target node defined", node.getId()), node.getId()));
                } else if (model.getNodes().stream().noneMatch(n -> n.getId().equals(connection.getTargetNode()))) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Target node '%s' does not exist in the model, but the connection of node '%s' tries to point to this non-existent node", connection.getTargetNode(), node.getId()), node.getId()));
                }
            });
        }
    }

    private void identifyOrphanedNodes() {
        List<String> roots = identifyNumberOfRoots(model);

        if (roots.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError("Model has no roots - it is cyclic and therefore invalid. The process must be a single continuous process with one root.", FULL_PROCESS));
            return;
        }

        if (roots.size() > 1) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Model has multiple roots: %s. The process must be a single continuous process with one root.", String.join(", ", roots)), FULL_PROCESS));
        }
    }

    private void validateNodeConnectionsRules(ElementNode node) {
        var incomingConnections = model.getNodes().stream()
                .filter(n -> n.getConnectedTo() != null)
                .filter(n -> n.getConnectedTo().stream().anyMatch(c -> c.getTargetNode().equals(node.getId())))
                .toList();
        if (node.getElementType().equals(START_EVENT)) {
            if (!incomingConnections.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Start event node '%s' has incoming connections. A start event can only have outgoing connections.", node.getId()), node.getId()));
            }
            if (node.getConnectedTo() == null || node.getConnectedTo().isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Start event node '%s' has no outgoing connections. A start event must have at least one outgoing connection.", node.getId()), node.getId()));
            }
        } else if (node.getElementType().equals(END_EVENT)) {
            if (node.getConnectedTo() != null && !node.getConnectedTo().isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("End event node '%s' has outgoing connections. An end event can only have incoming connections.", node.getId()), node.getId()));
            }
            if (incomingConnections.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("End event node '%s' has no incoming connections. An end event must have at least one incoming connection.", node.getId()), node.getId()));
            }
        } else if (node.getElementType().endsWith(GATEWAY_SUFFIX)) {
            // Gateway can either be a split (one incoming, multiple outgoing) or a merge (multiple incoming, one outgoing), but not both at the same time
            if (incomingConnections.size() > 1 && node.getConnectedTo().size() > 1) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has multiple incoming connections (%d) and multiple outgoing connections (%d). A gateway node can either be a split (one incoming, multiple outgoing) or a merge (multiple incoming, one outgoing), but not both at the same time. Create a separate gateway node for split and merge functionality.", node.getId(), incomingConnections.size(), node.getConnectedTo().size()), node.getId()));
            }
            // Validate that default node and conditions are mapped to existing nodes
            String defaultTargetNodeName = node.findInput("default").map(ElementNodeInput::getValue).orElse(null);
            if(StringUtils.isBlank(defaultTargetNodeName)) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' is missing a 'default' input specifying the default target node.", node.getId()), node.getId()));
            } else if(node.getConnectedTo().stream().noneMatch(c -> c.getTargetNode().equals(defaultTargetNodeName))) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has a default target node '%s' which is not among its outgoing connections. The 'default' input must specify target node from one of the outgoing connections.", node.getId(), defaultTargetNodeName), node.getId()));
            }
            var conditions = convertStringToMap(node.findInput("conditions").map(ElementNodeInput::getValue).orElse("{}"));
            for (String conditionTargetNode : conditions.keySet()) {
                if (node.getConnectedTo().stream().noneMatch(c -> c.getTargetNode().equals(conditionTargetNode))) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has a condition target node '%s' which is not among its outgoing connections. All condition target nodes must be one of the outgoing connections.", node.getId(), conditionTargetNode), node.getId()));
                }
            }
        } else {
            if (incomingConnections.size() != 1 && node.getConnectedTo().size() != 1) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' of type '%s' has %d incoming connections and %d outgoing connections. This type of node must have exactly one incoming and one outgoing connection.", node.getId(), node.getElementType(), incomingConnections.size(), node.getConnectedTo().size()), node.getId()));
            }
        }
    }

    private void validateCodeGenInputs(ProcessState state) {
        ElementNode node = state.currentNode;
        Map<String, Set<PayloadVariable>> payload = state.payload;

        if (node.getInputs() != null) {
            node.getInputs().forEach(input -> {
                if (input.getVariableSource().equals(CONSTANT.toString())) {
                    validateConstantInput(node, input, payload);
                } else if (input.getVariableSource().equals(EXPRESSION.toString())) {
                    validateExpressionInput(node, input, payload);
                } else if (input.getVariableSource().equals(SCRIPT.toString())) {
                    validateScriptInput(node, input, payload);
                } else if (input.getVariableSource().equals(NODE.toString())) {
                    validateNodeInput(node, input, payload);
                }
            });
        }

        Set<PayloadVariable> currentNodeOutputs = payload.getOrDefault(node.getId(), new HashSet<>());
        Set<String> currentNodeOutputsNames = currentNodeOutputs.stream()
                .map(PayloadVariable::getName)
                .collect(Collectors.toSet());

        // Get component generated outputs
        final var componentOutputVars = componentLibrary.getComponentByName(node.getElementType())
                .map(BpmnComponent::getGeneratedOutputs)
                .orElse(Collections.emptyList())
                .stream()
                .filter(outputVar -> !currentNodeOutputsNames.contains(outputVar.getName()))
                .map(outputVar ->  new PayloadVariable(outputVar.getName(), outputVar.getType().toString()))
                .toList();

        currentNodeOutputs.addAll(componentOutputVars);
        payload.put(node.getId(), currentNodeOutputs);
    }

    private void validateConstantInput(ElementNode node, ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        String constantValue = input.getValue();

        // Do not allow variable writes in constant inputs
        List<PayloadVariable> writtenVariables = retrieveVariablesWithPattern(constantValue, VAR_WRITE_PATTERN);
        if (!writtenVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value stores variables [%s] using setVariable(). CONSTANT inputs cannot write variables. Change its type to SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    input.getName(), node.getId(), String.join(", ", writtenVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        // Do not allow variable reads in constant inputs
        List<PayloadVariable> readVariables = retrieveVariablesWithPattern(constantValue, VAR_READ_PATTERN);
        if (!readVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value reads variables [%s] using getVariable(). CONSTANT inputs cannot read variables. Change its type to EXPRESSION or SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    input.getName(), node.getId(), String.join(", ", readVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        // Do not allow global variable reads in constant inputs
        List<PayloadVariable> readGlobalVariables = retrieveGlobalVariables(constantValue, globalVariableLibrary);
        if (!readGlobalVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value reads global variables [%s] using getGlobalVariable(). CONSTANT inputs cannot read global variables. Change its type to EXPRESSION or SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    input.getName(), node.getId(), String.join(", ", readGlobalVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }
    }

    private void validateExpressionInput(ElementNode node, ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        String expression = input.getValue();

        // Do not allow variable writes in constant inputs
        List<PayloadVariable> writtenVariables = retrieveVariablesWithPattern(expression, VAR_WRITE_PATTERN);
        if (!writtenVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value stores variables [%s] using setVariable(). EXPRESSION inputs cannot write variables. Change its type to SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    input.getName(), node.getId(), String.join(", ", writtenVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        validateVariableReads(node, input, payload);
        expression = resolveVariableReads(expression);

        validateGlobalVariableReads(node, input);
        expression = resolveGlobalVariableReads(expression, globalVariableLibrary, true);

        List<String> expressionsToValidate = new ArrayList<>();
        if (node.getElementType().endsWith(GATEWAY_SUFFIX) && input.getName().equals("conditions")) {
            var gatewayConditions = convertStringToMap(expression).values();
            expressionsToValidate.addAll(gatewayConditions);
        } else {
            expressionsToValidate.add(expression);
        }

        for (String expr : expressionsToValidate) {
            if (StringUtils.isBlank(expr)) {
                continue;
            }
            validateExpression(node, input, expr);
        }
    }

    private void validateExpression(ElementNode node, ElementNodeInput input, String expression) {
        if (expression.startsWith("return ")) {
            expression = expression.substring(7).trim();
        }
        if (expression.startsWith("/")) {
            expression = expression.substring(1);
        }
        // If the expression contains interpolation syntax or it is a json object, wrap it with additional quotes to parse it as a GString or PropertyExpression
        boolean addedQuotes = false;
        if (expression.contains("${") || expression.startsWith("{") && expression.endsWith("}")) {
            expression = "\"" + expression + "\"";
            addedQuotes = true;
        }

        try {
            AstBuilder astBuilder = new AstBuilder();
            List<ASTNode> astNodes;
            try {
                astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, expression);
            } catch (Exception e) {
                // Edge case: Parsing fails for scripts that contain interpolation values because of additional quotes so remove them and parse again
                if (addedQuotes) {
                    expression = expression.substring(1, expression.length() - 1);
                }
                astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, expression);
            }
            var astNode = astNodes.get(0);
            List<Statement> statements = ((BlockStatement) astNode).getStatements();

            if (statements.size() == 1) {
                Statement statement = statements.get(0);
                if (statement instanceof ExpressionStatement) {
                    Expression expr = ((ExpressionStatement) statement).getExpression();
                    if (!isValidExpressionType(expr)) {
                        invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value is a '%s' expression which is not allowed. EXPRESSION inputs must have single-line expressions. Change its source type to CONSTANT or SCRIPT, or provide a valid EXPRESSION value.",
                                input.getName(), node.getId(), expr.getClass().getSimpleName()), node.getId()));
                    }
                }
            } else {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value appears to be a Groovy script. EXPRESSION inputs must have single-line expressions. Change its source type to SCRIPT or provide a valid EXPRESSION value.",
                        input.getName(), node.getId()), node.getId()));
            }
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value could not be parsed as a valid Groovy expression and threw an error: %s. EXPRESSION inputs must have single-line expressions. Change its source type to SCRIPT or provide a valid EXPRESSION value.",
                    input.getName(), node.getId(), e.getMessage()), node.getId()));
        }
    }

    private boolean isValidExpressionType(Expression expr) {
        return expr instanceof BinaryExpression ||
                expr instanceof VariableExpression ||
                expr instanceof GStringExpression ||
                expr instanceof PropertyExpression ||
                expr instanceof CastExpression ||
                expr instanceof ConstantExpression;
    }


    private void validateScriptInput(ElementNode node, ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        String script = input.getValue();

        // Check for variable writes
        List<PayloadVariable> writtenVariables = retrieveVariablesWithPattern(script, VAR_WRITE_PATTERN);
        script = resolveVariableWrites(script);

        // Check for variable reads
        boolean isScriptValid = validateVariableReads(node, input, payload) && validateGlobalVariableReads(node, input);
        script = resolveVariableReads(script);
        script = resolveGlobalVariableReads(script, globalVariableLibrary, false);

        // Replace throw(...) but don't use valid Groovy exception syntax as it would throw actual exception during evaluation
        script = resolveErrorThrows(script, "errorMessagePlaceholder = $1");

        // Groovy script validation will fail if there are variable read errors so only proceed if there are no such errors
        if (isScriptValid) {
            String globalVarInitScript = buildGlobalVarsInitScript();
            script = globalVarInitScript + buildDummyPayload(input, payload) + script;

            if (parseGroovyScript(node, input.getName(), script)) {
                validateGroovyScript(node, input.getName(), script);
            }
        }

        // Store written variables per node - do it after building dummy payload to avoid including vars stored in the current node in the dummy payload
        Set<PayloadVariable> currentNodeOutputs = payload.getOrDefault(node.getId(), new HashSet<>());
        currentNodeOutputs.addAll(writtenVariables);
        payload.put(node.getId(), currentNodeOutputs);
    }

    private void validateNodeInput(ElementNode node, ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        String sourceNodeId = input.getValue();
        String variableName = input.getName();

        if (!payload.containsKey(sourceNodeId)) {
            invalidMessages.add(new IntermediateModelValidationError(
                    String.format("Input '%s' in node '%s' references source node '%s' which does not exist or has not been processed yet.",
                            variableName, node.getId(), sourceNodeId), node.getId()));
            return;
        }

        Set<PayloadVariable> sourceNodeOutputs = payload.get(sourceNodeId);
        boolean variableExists = sourceNodeOutputs.stream()
                .anyMatch(v -> v.getName().equals(variableName));

        if (!variableExists) {
            invalidMessages.add(new IntermediateModelValidationError(
                    String.format("Input '%s' in node '%s' tries to read from node '%s' which does not output this variable. Available outputs: [%s]",
                            variableName, node.getId(), sourceNodeId,
                            sourceNodeOutputs.stream().map(PayloadVariable::getName).collect(Collectors.joining(", "))),
                    node.getId()));
        }
    }

    private boolean validateVariableReads(ElementNode node, ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        boolean isScriptValid = true;
        Matcher readMatcher = VAR_READ_PATTERN.matcher(input.getValue());
        while (readMatcher.find()) {
            String variableName = readMatcher.group(1);
            String sourceNodeId = readMatcher.group(2);

            // If reading from a specific node (sourceNodeId specified), validate if input exists in payload or in source node outputs
            boolean foundInSourceNode = false;
            if (StringUtils.isNotBlank(sourceNodeId)) {
                Set<PayloadVariable> sourceNodeOutputs = payload.getOrDefault(sourceNodeId, Set.of());
                foundInSourceNode = sourceNodeOutputs.stream().anyMatch(x -> x.getName().equals(variableName) || variableName.startsWith(x.getName() + "."));
            }
            // Fallback: check if variable exists in the starting payload
            Set<PayloadVariable> startingPayload = payload.get("startingPayload");
            boolean foundInStartingPayload = startingPayload.stream().anyMatch(x -> x.getName().equals(variableName) || variableName.startsWith(x.getName() + "."));

            if (!foundInSourceNode && !foundInStartingPayload) {
                invalidMessages.add(new IntermediateModelValidationError(
                        String.format("%s in node '%s' reads variable '%s' from " + (StringUtils.isNotBlank(sourceNodeId)
                                        ? "node '%s' which does not store or output this variable. "
                                        : "the starting payload which does not store it. ") +
                                        "Ensure this variable is provided in the starting payload, generated as a source node output, or written in a previous node's script using setVariable().",
                                input.getName(), node.getId(), variableName, sourceNodeId), node.getId()));
                isScriptValid = false;
            }
        }
        return isScriptValid;
    }

    private boolean validateGlobalVariableReads(ElementNode node, ElementNodeInput input) {
        boolean isScriptValid = true;
        Matcher globalVarMatcher = GLOBAL_VAR_READ_PATTERN.matcher(input.getValue());
        while (globalVarMatcher.find()) {
            String variableName = globalVarMatcher.group(1);
            String arguments = globalVarMatcher.group(2);

            // Check if global variable exists in the global variable library
            var globalVar = globalVariableLibrary.getVariableByName(variableName);
            if (globalVar.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(
                        String.format("%s in node '%s' reads global variable '%s' which does not exist in the global variable library. " +
                                        "Change this input to a global variable which is in the library, or to another input type such as CONSTANT, EXPRESSION, SCRIPT or NODE if appropriate.",
                                input.getName(), node.getId(), variableName), node.getId()));
                isScriptValid = false;
            } else {
                // Verify that all required arguments are provided
                var requiredArgs = globalVar.get().getArguments();
                if (StringUtils.isNotBlank(arguments) && arguments.trim().split(COMMA_DELIMITER).length != requiredArgs.size()) {
                    invalidMessages.add(new IntermediateModelValidationError(
                            String.format("%s in node '%s' reads global variable '%s' but the number of provided arguments (%d) does not match the required number of arguments (%d). " +
                                            "Ensure to provide all required arguments when reading this global variable.",
                                    input.getName(), node.getId(), variableName, arguments.trim().split(COMMA_DELIMITER).length, requiredArgs.size()), node.getId()));
                    isScriptValid = false;
                }
            }
        }
        return isScriptValid;
    }

    private String buildDummyPayload(ElementNodeInput input, Map<String, Set<PayloadVariable>> payload) {
        StringBuilder fullScript = new StringBuilder("def payload = [:];\n");

        // Accumulate variables from the starting payload with current payload
        // Sort by depth to ensure parent objects are created before child properties
        List<PayloadVariable> sortedPayloadVariables = payload.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparingInt(var -> var.getName().split("\\.").length))
                .toList();

        Set<String> initializedVars = new HashSet<>();

        for (PayloadVariable variable : sortedPayloadVariables) {
            String[] parts = variable.getName().split("\\.");
            StringBuilder currentGroovyVar = new StringBuilder();

            for (int i = 0; i < parts.length; i++) {
                currentGroovyVar.append(i > 0 ? "." : "").append(parts[i]);
                String fullIntermediateVar = currentGroovyVar.toString();

                if (!initializedVars.contains(fullIntermediateVar)) {
                    boolean hasChildren = i < parts.length - 1 || sortedPayloadVariables.stream().anyMatch(var -> var.getName().startsWith(fullIntermediateVar + "."));

                    if (hasChildren) {
                        fullScript.append("payload.").append(fullIntermediateVar).append(" = payload.").append(fullIntermediateVar).append(" ?: [:];\n");
                    } else {
                        String dummyValue = switch (variable.getType().toLowerCase()) {
                            case "integer" -> "123";
                            case "boolean" -> "true";
                            case "float" -> "123.45";
                            case "array" -> "[]";
                            case "object" -> "[:]";
                            default -> "'default_dummy_value_for_" + variable.getName() + "'";
                        };

                        fullScript.append("payload.").append(fullIntermediateVar).append(" = ").append(dummyValue).append(";\n");
                    }
                    initializedVars.add(fullIntermediateVar);
                }
            }
        }

        // Output script can use "response" output variable so ensure it's initialized
        if (input.getName().equals("outputScript") && !initializedVars.contains("response")) {
            fullScript.append("def response = [:];\n");
            fullScript.append("response.statusLine = [:]\nresponse.statusLine = [:]\nresponse.statusLine.statusCode = 200\n");
            fullScript.append("response.request = [:]\nresponse.request.uri = 'http://example.com/main/api'\n");
            fullScript.append("response.entity = [:]\n");
        }

        return fullScript.append("\n").toString();
    }

    public String buildGlobalVarsInitScript() {
        StringBuilder script = new StringBuilder();
        Set<String> processedLines = new HashSet<>();

        for (var globalVar : globalVariableLibrary.getComponents()) {
            String initScript = globalVar.getInitScript();
            if (initScript != null && !initScript.isBlank()) {
                String[] lines = initScript.split("\n");
                for (String line : lines) {
                    if (!processedLines.contains(line)) {
                        script.append(line).append("\n");
                        processedLines.add(line);
                    }
                }
            }
        }
        return script.toString();
    }

    private boolean parseGroovyScript(ElementNode node, String inputName, String script) {
        try {
            shell.parse(script);
            return true;
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("%s code for node '%s' is not a valid Groovy code or expression. Exception: '%s'", inputName, node.getId(), e), node.getId()));
            return false;
        }
    }

    private void validateGroovyScript(ElementNode node, String inputName, String script) {
        try {
            shell.evaluate(script);
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a SCRIPT input but its value could not be evaluated. " +
                            "SCRIPT inputs must contain executable code that may read or write variables using getVariable() and setVariable(). Exception: '%s'",
                    inputName, node.getId(), e.getMessage()), node.getId()));
        }
    }

    private void traverseGraphInExecutionOrder(List<Consumer<ProcessState>> validationFunctions, Map<String, Set<PayloadVariable>> startingPayload) {
        List<String> roots = identifyNumberOfRoots(model);
        if (roots.size() != 1) {
            return;
        }

        String startNodeId = roots.get(0);
        ElementNode startNode = model.getNodeById(startNodeId).orElse(null);

        if (startNode == null) {
            return;
        }

        Stack<ProcessState> stack = new Stack<>();
        stack.push(new ProcessState(startNode, new ArrayList<>(List.of(startNode.getId())), startingPayload));

        while (!stack.isEmpty()) {
            ProcessState state = stack.pop();
            ElementNode currentNode = state.currentNode;
            List<String> currentPath = state.path;
            Map<String, Set<PayloadVariable>> payload = state.payload;

            for (var nodeValidationFunction : validationFunctions) {
                nodeValidationFunction.accept(state);
            }

            if (currentNode.getConnectedTo() != null) {
                for (ElementConnection connection : currentNode.getConnectedTo()) {
                    ElementNode targetNode = model.getNodeById(connection.getTargetNode()).orElse(null);

                    if (targetNode != null && !currentPath.contains(targetNode.getId())) {
                        List<String> newPath = new ArrayList<>(currentPath);
                        newPath.add(targetNode.getId());
                        stack.push(new ProcessState(targetNode, newPath, deepCopyPayload(payload)));
                    }
                }
            }
        }

    }

    private Map<String, Set<PayloadVariable>> deepCopyPayload(Map<String, Set<PayloadVariable>> payload) {
        return payload.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new HashSet<>(e.getValue())
                ));
    }

    public static class ProcessState {
        ElementNode currentNode;
        List<String> path;
        Map<String, Set<PayloadVariable>> payload;

        ProcessState(ElementNode currentNode, List<String> path, Map<String, Set<PayloadVariable>> payload) {
            this.currentNode = currentNode;
            this.path = path;
            this.payload = payload;
        }
    }
}
