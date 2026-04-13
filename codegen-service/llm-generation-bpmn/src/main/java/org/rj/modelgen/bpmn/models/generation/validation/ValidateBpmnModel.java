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
import org.rj.modelgen.bpmn.component.common.BpmnComponentVariableType;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType;
import org.rj.modelgen.bpmn.intrep.model.*;
import org.rj.modelgen.bpmn.intrep.model.rendering.ConditionalGateway;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.*;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.*;
import static org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType.*;
import static org.rj.modelgen.bpmn.models.generation.validation.BpmnScriptUtils.*;
import static org.rj.modelgen.llm.util.ValidationUtils.identifyNumberOfRoots;

public class ValidateBpmnModel {

    private static final String FULL_PROCESS = "full_process";
    private static final String COMMA_DELIMITER = ",";
    private static final List<String> NODES_TO_IGNORE = List.of(PROCESS_CONFIG);

    private BpmnIntermediateModel model;
    private BpmnGlobalVariableLibrary globalVariableLibrary;
    private BpmnComponentLibrary componentLibrary;
    private List<IntermediateModelValidationError> invalidMessages;
    private final GroovyShell shell = new GroovyShell();

    public ValidateBpmnModel(BpmnComponentLibrary componentLibrary, BpmnGlobalVariableLibrary globalVariableLibrary) {
        this.globalVariableLibrary = globalVariableLibrary;
        this.componentLibrary = componentLibrary;
    }

    public List<IntermediateModelValidationError> validate(BpmnIntermediateModel model, Set<PayloadVariable> startingPayload) {
        this.model = model;
        invalidMessages = new ArrayList<>();
        for (ElementNode node : model.getNodes()) {
            validateNodeNames(node);
            validateRequiredInputs(node);
            validateNodeConnections(node);
            validateNodeConnectionsRules(node);
        }
        identifyOrphanedNodes();

        // Code validation requires traversing a valid graph in execution order so execute it after successful graph structure validation
        if (invalidMessages.isEmpty()) {
            Map<String, List<ElementNode>> predecessors = new HashMap<>(); // Key: node ID, Value: list of immediate predecessor nodes
            Map<String, Set<PayloadVariable>> inVars = new HashMap<>(); // Key: node ID, Value: set of entry variables available to the node before it executes
            Map<String, Set<PayloadVariable>> outVars = new HashMap<>(); // Key: node ID, Value: set of exit variables available after the node executes

            for (ElementNode node : model.getNodes()) {
                inVars.put(node.getId(), new HashSet<>());
                outVars.put(node.getId(), new HashSet<>());

                // Compute predecessors
                predecessors.putIfAbsent(node.getId(), new ArrayList<>());
                List<ElementConnection> outgoingConnections = node.getConnectedTo() == null ? new ArrayList<>() : node.getConnectedTo().stream().toList();
                for (ElementConnection connection : outgoingConnections) {
                    model.getNodes().stream()
                            .filter(n -> n.getId().equals(connection.getTargetNode()))
                            .findFirst()
                            .ifPresent(targetNode ->
                                    predecessors.computeIfAbsent(targetNode.getId(), k -> new ArrayList<>()).add(node));
                }

            }
            traverseGraphAndValidateInputs(predecessors, inVars, outVars, startingPayload);
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

    private void validateNodeInputValuesAndVariableSourceExist(ElementNode node, ElementNodeInput input, Collection<IntermediateModelValidationError> invalidInputMessages) {
        if (input.hasProperties()) {
            // Validate all properties of object type input
            for (ElementNodeInput property : input.getProperties()) {
                validateNodeInputValuesAndVariableSourceExist(node, property, invalidInputMessages);
            }
        } else {
            // Validate primitive type input
            if (StringUtils.isBlank(input.getName())) {
                invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has input with null or empty name", node.getId()), node.getId()));
            } else {
                if (input.getValue() == null) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null value", node.getId(), input.getName()), node.getId()));
                }
                if (StringUtils.isBlank(input.getVariableSource())) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null or empty variableSource. It should be CONSTANT, EXPRESSION, SCRIPT, NODE or GLOBAL.", node.getId(), input.getName()), node.getId()));
                }
            }
        }
    }

    private void validateNodeInputValuesMatchDefinition(ElementNode node, List<ElementNodeInput> inputs, BpmnComponent.InputVariable inputDefinition, String inputPath) {
        final String path = (inputPath == null || inputPath.isBlank()) ? inputDefinition.getName() : inputPath;

        // If the input is mandatory, it must be present
        if (inputs == null || inputs.isEmpty()) {
            if (inputDefinition.isMandatory()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' is missing mandatory input '%s'", node.getId(), path), node.getId()));
            }
            return;
        }
        // Input must not be defined multiple times unless input definition is an array
        if (inputs.size() > 1 && !inputDefinition.getType().equals(BpmnComponentVariableType.Array)) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has multiple definitions of input '%s'", node.getId(), path), node.getId()));
            return;
        }

        for (ElementNodeInput input : inputs) {
            if (input.hasProperties()) {
                // If input is an object, input definition has to be an object type as well
                if (inputDefinition.getProperties().isEmpty()) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an object type input '%s' but the input definition is a primitive type", node.getId(), path), node.getId()));
                    continue;
                }
                // If input is an object, validate its properties recursively
                for (var inputDefProp : inputDefinition.getProperties()) {
                    final String childPath = path + "." + inputDefProp.getName();
                    var inputProperty = input.getProperties().stream()
                            .filter(x -> x.getName().equals(inputDefProp.getName()))
                            .findFirst();

                    if (inputProperty.isEmpty()) {
                        if (inputDefProp.isMandatory()) {
                            invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' is missing mandatory property '%s'", node.getId(), childPath), node.getId()));
                        }
                        continue;
                    }
                    validateNodeInputValuesMatchDefinition(node, List.of(inputProperty.get()), inputDefProp, childPath);
                }
            } else { // Primitive type input
                // If input is primitive type, input definition hast to be primitive type as well
                if (!inputDefinition.getProperties().isEmpty()) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has a primitive type input '%s' but the input definition is an object type with properties", node.getId(), path), node.getId()));
                    continue;
                }
                // Input value can be empty only if it's explicitly provided as empty string in the runbook or if its default value is empty string
                if (input.getValue().isEmpty() && !input.getIsProvided() && (inputDefinition.getDefaultValue() == null || !inputDefinition.getDefaultValue().isEmpty())) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an empty value for input '%s'", node.getId(), path), node.getId()));
                }
                // If the input is enum and constant, it must have a valid value
                if (inputDefinition.getAllowedValues() != null && !inputDefinition.getAllowedValues().isEmpty() && input.getVariableSource().equals(CONSTANT.toString()) && !inputDefinition.getAllowedValues().contains(input.getValue())) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an invalid value for input '%s'. Allowed values are: %s", node.getId(), path, String.join(", ", inputDefinition.getAllowedValues())), node.getId()));
                }
                // If the input's source type is GLOBAL, then global variable library must contain the input variable
                if (input.getVariableSource().equals(GLOBAL.toString()) && globalVariableLibrary.getVariableByName(input.getValue()).isEmpty()) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an input '%s' which tries to use a global variable '%s' that does not exist in the global variable library",
                            node.getId(), path, input.getValue()), node.getId()));
                }
                // Input's source type must be among the component allowed source types
                if (!inputDefinition.isAllowedInputSourceType(input.getVariableSource())) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an input '%s' with invalid variable source type '%s'. Allowed source types for this input are: %s",
                            node.getId(), path, input.getVariableSource(), String.join(", ", inputDefinition.getAllowedInputSourceTypes().stream().map(BpmnComponentInputSourceType::toString).toList())), node.getId()));
                }
            }
        }
    }

    private void validateRequiredInputs(ElementNode node) {
        final Collection<IntermediateModelValidationError> invalidInputMessages = new ArrayList<>();
        final var component = componentLibrary.getComponentByName(node.getElementType());
        if (component.isEmpty()) return;   // Unknown action type, should probably never happen by the time we reach this point
        if (node.getInputs() == null) return;

        for (ElementNodeInput nodeInput : node.getInputs()) {
            validateNodeInputValuesAndVariableSourceExist(node, nodeInput, invalidInputMessages);
        }

        if (!invalidInputMessages.isEmpty()) {
            // All inputs must have valid values before proceeding with further validation
            invalidMessages.addAll(invalidInputMessages);
            return;
        }

        for (var inputDefinition : component.get().getRequiredInputs()) {
            List<ElementNodeInput> inputs = node.getInputs() == null
                    ? new ArrayList<>()
                    : node.getInputs().stream()
                        .filter(nodeInput -> nodeInput.getName().equals(inputDefinition.getName()))
                        .toList();

            validateNodeInputValuesMatchDefinition(node, inputs, inputDefinition, inputDefinition.getName());
        }
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
        // Identify number of roots but ignore processConfig node
        List<String> roots = identifyNumberOfRoots(model, node -> !NODES_TO_IGNORE.contains(node.getElementType()));

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
            if (node instanceof ConditionalGateway conditionalGateway) {
                String defaultTargetNodeName = conditionalGateway.getDefaultTargetNodeId();
                Map<String, String> conditions = conditionalGateway.getConditions();

                // Validate that default node and conditions are mapped to existing nodes. Merge gateways with one outgoing connection do not need to have default node or conditions defined
                if (node.getConnectedTo().size() > 1) {
                    if(StringUtils.isBlank(defaultTargetNodeName)) {
                        invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' is missing a 'default' input specifying the default target node.", node.getId()), node.getId()));
                    } else if(node.getConnectedTo().stream().noneMatch(c -> c.getTargetNode().equals(defaultTargetNodeName))) {
                        invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has a default target node '%s' which is not among its outgoing connections. The 'default' input must specify target node from one of the outgoing connections.", node.getId(), defaultTargetNodeName), node.getId()));
                    }
                }

                for (String conditionTargetNode : conditions.keySet()) {
                    if (node.getConnectedTo().stream().noneMatch(c -> c.getTargetNode().equals(conditionTargetNode))) {
                        invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has a condition target node '%s' which is not among its outgoing connections. All condition target nodes must be one of the outgoing connections.", node.getId(), conditionTargetNode), node.getId()));
                    }
                }
            }
        } else if (node.getElementType().equals(PROCESS_CONFIG)) {
            if (!incomingConnections.isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Process configuration node '%s' has incoming connections. A process configuration must be an orphan node.", node.getId()), node.getId()));
            }
            if (!node.getConnectedTo().isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Process configuration node '%s' has outgoing connections. A process configuration must be an orphan node.", node.getId()), node.getId()));
            }
        }
        else {
            if (incomingConnections.size() != 1 && node.getConnectedTo().size() != 1) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' of type '%s' has %d incoming connections and %d outgoing connections. This type of node must have exactly one incoming and one outgoing connection.", node.getId(), node.getElementType(), incomingConnections.size(), node.getConnectedTo().size()), node.getId()));
            }
        }
    }

    private void validateConstantInput(ElementNode node, ElementNodeInput input, String inputPath) {
        String constantValue = input.getValue().strip();

        // Do not allow variable writes in constant inputs
        List<PayloadVariable> writtenVariables = retrieveWriteVariables(constantValue);
        if (!writtenVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value stores variables [%s] using setVariable(). CONSTANT inputs cannot write variables. Change its type to SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    inputPath, node.getId(), String.join(", ", writtenVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        // Do not allow variable reads in constant inputs
        List<PayloadVariable> readVariables = retrieveReadVariables(constantValue);
        if (!readVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value reads variables [%s] using getVariable(). CONSTANT inputs cannot read variables. Change its type to EXPRESSION or SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    inputPath, node.getId(), String.join(", ", readVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        // Do not allow global variable reads in constant inputs
        List<PayloadVariable> readGlobalVariables = retrieveGlobalVariables(constantValue, globalVariableLibrary);
        if (!readGlobalVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a CONSTANT input but its value reads global variables [%s] using getGlobalVariable(). CONSTANT inputs cannot read global variables. Change its type to EXPRESSION or SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    inputPath, node.getId(), String.join(", ", readGlobalVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }
    }

    private void validateExpressionInput(ElementNode node, ElementNodeInput input, String inputPath, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        String expression = input.getValue().strip();

        // Do not allow variable writes in constant inputs
        List<PayloadVariable> writtenVariables = retrieveWriteVariables(expression);
        if (!writtenVariables.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value stores variables [%s] using setVariable(). EXPRESSION inputs cannot write variables. Change its type to SCRIPT (if the input definition allows this type) or provide a valid CONSTANT value.",
                    inputPath, node.getId(), String.join(", ", writtenVariables.stream().map(PayloadVariable::getName).toList())), node.getId()));
        }

        validateVariableReads(node, input, inputPath, startingPayload, nodePayload);
        expression = resolveVariableReadsAsPayloadVar(expression, true);

        validateGlobalVariableReads(node, input, inputPath);
        expression = resolveGlobalVariableReads(expression, globalVariableLibrary, true);
        validateExpression(node, inputPath, expression);
    }

    private void validateExpression(ElementNode node, String inputPath, String expression) {
        if (expression.startsWith("return ")) {
            expression = expression.substring(7).trim();
        }
        if (expression.startsWith("/")) {
            expression = expression.substring(1);
        }
        // If the expression contains interpolation syntax or it is a json object, wrap it with additional quotes to parse it as a GString or PropertyExpression
        if (!expression.startsWith("\"") && !expression.endsWith("\"") && (expression.contains("${") || expression.startsWith("{") && expression.endsWith("}"))) {
            expression = "\"" + expression + "\"";
        }

        try {
            AstBuilder astBuilder = new AstBuilder();
            List<ASTNode> astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, expression);

            var astNode = astNodes.get(0);
            List<Statement> statements = ((BlockStatement) astNode).getStatements();

            if (statements.size() == 1) {
                Statement statement = statements.get(0);
                if (statement instanceof ExpressionStatement) {
                    Expression expr = ((ExpressionStatement) statement).getExpression();
                    if (!isValidExpressionType(expr)) {
                        invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value is a '%s' expression which is not allowed. EXPRESSION inputs must have single-line expressions. Change its source type to CONSTANT or SCRIPT, or provide a valid EXPRESSION value.",
                                inputPath, node.getId(), expr.getClass().getSimpleName()), node.getId()));
                    }
                }
            } else {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value appears to be a Groovy script. EXPRESSION inputs must have single-line expressions. Change its source type to SCRIPT or provide a valid EXPRESSION value.",
                        inputPath, node.getId()), node.getId()));
            }
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is an EXPRESSION input but its value could not be parsed as a valid Groovy expression and threw an error: %s. EXPRESSION inputs must have single-line expressions. Change its source type to SCRIPT or provide a valid EXPRESSION value.",
                    inputPath, node.getId(), e.getMessage()), node.getId()));
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

    private void validateScriptInput(ElementNode node, ElementNodeInput input, String inputPath, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        String script = input.getValue();

        // Check for variable writes
        List<PayloadVariable> writtenVariables = retrieveWriteVariables(script);
        script = resolveVariableWrites(script);

        // Check for variable reads
        boolean isScriptValid = validateVariableReads(node, input, inputPath, startingPayload, nodePayload) && validateGlobalVariableReads(node, input, inputPath);
        script = resolveVariableReadsAsPayloadVar(script, false);
        script = resolveGlobalVariableReads(script, globalVariableLibrary, false);

        // Replace throw(...) but don't use valid Groovy exception syntax as it would throw actual exception during evaluation
        script = resolveErrorThrows(script, "errorMessagePlaceholder = $1");

        // Groovy script validation will fail if there are variable read errors so only proceed if there are no such errors
        if (isScriptValid) {
            String globalVarInitScript = buildGlobalVarsInitScript();
            script = globalVarInitScript + buildDummyPayload(input, startingPayload, nodePayload) + script;

            if (parseGroovyScript(node, inputPath, script)) {
                validateGroovyScript(node, inputPath, script);
            }
        }
    }

    private void validateNodeInput(ElementNode node, ElementNodeInput input, String inputPath, Set<PayloadVariable> nodePayload) {
        String sourceNodeId = input.getValue();

        boolean foundInPayload = nodePayload.stream().anyMatch(payloadVar -> payloadVar.getName().equals(input.getName()));
        if (!foundInPayload) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' tries to read variable '%s' sourced from node '%s', but this variable is not available or the source node hasn't been processed yet.",
                    inputPath, node.getId(), input.getName(), sourceNodeId),
                    node.getId()));
        }
    }

    private boolean validateVariableReads(ElementNode node, ElementNodeInput input, String inputPath, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        boolean isInputValid = true;
        Matcher readMatcher = VAR_READ_PATTERN.matcher(input.getValue());
        while (readMatcher.find()) {
            String variableName = readMatcher.group(1);
            String sourceNodeId = readMatcher.group(2);

            // Validate if the current node has access to the variable
            boolean foundInPayload = nodePayload.stream()
                    .anyMatch(payloadVar -> payloadVar.getName().equals(variableName) || variableName.startsWith(payloadVar.getName() + "."));

            // Automatically generated outputs are only available to immediate successor nodes, so nodePayload will not contain them (unless it's an immediate successor)
            // Therefore, if variable not found in nodePayload, check if source node produces this variable to provide more specific error message
            boolean foundInSourceOutputs = StringUtils.isNotBlank(sourceNodeId) &&
                    model.getNodeById(sourceNodeId)
                        .map(ElementNode::getElementType)
                        .map(this::getGeneratedOutputsByElementType)
                        .map(generatedOutputs -> generatedOutputs.stream()
                                .map(BpmnComponent.Variable::getName)
                                .anyMatch(outputName ->  outputName.equals(variableName)))
                        .orElse(false);

            // Fallback: check if variable exists in the starting payload
            boolean foundInStartingPayload = startingPayload.stream()
                    .anyMatch(payloadVar -> payloadVar.getName().equals(variableName) || variableName.startsWith(payloadVar.getName() + "."));

            if (!foundInPayload) {
                if (foundInSourceOutputs) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' tries to read variable '%s' from node '%s' which produces this variable as an automatically generated output, but '%s' is not an immediate successor of '%s'. " +
                                    "Automatically generated outputs are only available to the node directly after the producing node. " +
                                    "To use this value in '%s', add a scriptTask immediately after node '%s' that stores the value in shared state using setVariable(...) syntax.",
                            inputPath, node.getId(), variableName, sourceNodeId,
                            node.getId(), sourceNodeId,
                            node.getId(), sourceNodeId),
                            node.getId()));
                    isInputValid = false;

                } else if (!foundInStartingPayload) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("%s in node '%s' reads variable '%s' from " + (StringUtils.isNotBlank(sourceNodeId)
                                    ? "node '%s' which does not store or output this variable. "
                                    : "the starting payload which does not store it. ") +
                                    "Ensure this variable is provided in the starting payload, generated as a source node output, or written in a previous node's script using setVariable().",
                            inputPath, node.getId(), variableName, sourceNodeId), node.getId()));
                    isInputValid = false;
                }
            }
        }
        return isInputValid;
    }

    private boolean validateGlobalVariableReads(ElementNode node, ElementNodeInput input, String inputPath) {
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
                                inputPath, node.getId(), variableName), node.getId()));
                isScriptValid = false;
            } else {
                // Verify that all required arguments are provided
                var requiredArgs = globalVar.get().getArguments();
                if (StringUtils.isNotBlank(arguments) && arguments.trim().split(COMMA_DELIMITER).length != requiredArgs.size()) {
                    invalidMessages.add(new IntermediateModelValidationError(
                            String.format("%s in node '%s' reads global variable '%s' but the number of provided arguments (%d) does not match the required number of arguments (%d). " +
                                            "Ensure to provide all required arguments when reading this global variable.",
                                    inputPath, node.getId(), variableName, arguments.trim().split(COMMA_DELIMITER).length, requiredArgs.size()), node.getId()));
                    isScriptValid = false;
                }
            }
        }
        return isScriptValid;
    }

    private String buildDummyPayload(ElementNodeInput input, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        StringBuilder fullScript = new StringBuilder("def payload = [:];\n");

        // Accumulate variables from the starting payload with current payload
        // Sort by depth to ensure parent objects are created before child properties
        List<PayloadVariable> sortedPayloadVariables = Stream.concat(startingPayload.stream(), nodePayload.stream())
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
            fullScript.append("response.statusLine = [:]\nresponse.statusLine.statusCode = 200\n");
            fullScript.append("response.request = [:]\nresponse.request.uri = 'http://example.com/main/api'\n");
            fullScript.append("response.entity = [:]\n");
        }
        fullScript.append(buildAdditionalDummyPayload(input, startingPayload, nodePayload, initializedVars));

        return fullScript.append("\n").toString();
    }

    // No-op method to allow extending classes to add additional dummy payloads
    protected String buildAdditionalDummyPayload(ElementNodeInput input, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload, Set<String> initializedVars) {
        return "";
    }

    private String buildGlobalVarsInitScript() {
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

    private boolean parseGroovyScript(ElementNode node, String inputPath, String script) {
        try {
            shell.parse(script);
            return true;
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("%s code for node '%s' is not a valid Groovy code or expression. Exception: '%s'", inputPath, node.getId(), e), node.getId()));
            return false;
        }
    }

    private void validateGroovyScript(ElementNode node, String inputPath, String script) {
        try {
            shell.evaluate(script);
        } catch (Exception e) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Input '%s' in node '%s' is a SCRIPT input but its value could not be evaluated. " +
                            "SCRIPT inputs must contain executable code that may read or write variables using getVariable() and setVariable(). Exception: '%s'",
                    inputPath, node.getId(), e.getMessage()), node.getId()));
        }
    }

    // Data flow analysis
    private void traverseGraphAndValidateInputs(Map<String, List<ElementNode>> predecessors,
                                                Map<String, Set<PayloadVariable>> inVars,
                                                Map<String, Set<PayloadVariable>> outVars,
                                                Set<PayloadVariable> startingPayload) {

        List<String> roots = identifyNumberOfRoots(model, node -> !NODES_TO_IGNORE.contains(node.getElementType())); // Ignore processConfig node

        if (roots.size() == 1) {
            model.getNodeById(roots.get(0)).ifPresent(startNode -> {
                inVars.put(startNode.getId(), startingPayload);
            });
        }

        // Track which variables are automatically generated outputs per node type
        Map<String, Set<PayloadVariable>> generatedOutputsByElementType = new HashMap<>();
        for (BpmnComponent component : componentLibrary.getComponents()) {
            var outputs = getGeneratedOutputsByElementType(component.getName())
                    .stream()
                    .map(outVar -> new PayloadVariable(outVar.getName(), outVar.getType().toString()))
                    .collect(Collectors.toSet());
            generatedOutputsByElementType.put(component.getName(), outputs);
        }

        boolean changed;
        int maxIterations = 1_000_000;
        int iter = 0;
        do {
            changed = false;
            for (ElementNode node : model.getNodes()) {
                // IN[node] = union of OUT[predecessor] and autoGenOut[predecessor] for all predecessors
                Set<PayloadVariable> newInVars = new HashSet<>();
                // OUT[node] = union of OUT[predecessor] for all predecessors + variables written by this node
                Set<PayloadVariable> newOutVars = new HashSet<>();
                for (ElementNode pred : predecessors.getOrDefault(node.getId(), List.of())) {
                    newInVars.addAll(outVars.get(pred.getId()));
                    // Add generated outputs only from immediate predecessors and not propagate to successors down the line, as they are only available to immediate successor nodes
                    newInVars.addAll(generatedOutputsByElementType.getOrDefault(pred.getElementType(), Set.of()));

                    newOutVars.addAll(outVars.get(pred.getId()));
                }

                // Extract variables written by this node's inputs
                if (node.getInputs() != null) {
                    for (ElementNodeInput input : node.getInputs()) {
                        extractWrittenVariables(input, newOutVars);
                    }
                }

                // Check if anything changed
                if (!newInVars.equals(inVars.get(node.getId())) || !newOutVars.equals(outVars.get(node.getId()))) {
                    inVars.put(node.getId(), newInVars);
                    outVars.put(node.getId(), newOutVars);
                    changed = true;
                }
            }
            iter++;
        } while (changed && iter < maxIterations);

        if (changed) {
            throw new RuntimeException("Bpmn model validation failed: Data flow analysis did not converge within the maximum number of iterations.");
        }

        for (ElementNode node : model.getNodes()) {
            if (node.getInputs() != null) {
                var nodePayload = inVars.get(node.getId());

                String inputPath = "";
                for (ElementNodeInput input : node.getInputs()) {
                    validateInput(node, input, inputPath, startingPayload, nodePayload);
                }
            }
        }
    }

    private void extractWrittenVariables(ElementNodeInput input, Collection<PayloadVariable> writtenVariables) {
        if (input.hasProperties()) {
            for (ElementNodeInput property : input.getProperties()) {
                extractWrittenVariables(property, writtenVariables);
            }
        } else {
            if (input.getVariableSource().equals(SCRIPT.toString())) {
                writtenVariables.addAll(retrieveWriteVariables(input.getValue()));
            }
        }
    }

    private void validateInput(ElementNode node, ElementNodeInput input, String path, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        String childPath = path.isEmpty() ? input.getName() : path + "." + input.getName();
        if (input.hasProperties()) {
            for (ElementNodeInput property : input.getProperties()) {
                validateInput(node, property, childPath, startingPayload, nodePayload);
            }
        } else {
            validateInputValue(node, input, childPath, startingPayload, nodePayload);
        }
    }

    private void validateInputValue(ElementNode node, ElementNodeInput input, String path, Set<PayloadVariable> startingPayload, Set<PayloadVariable> nodePayload) {
        if (input.getVariableSource().equals(CONSTANT.toString())) {
            validateConstantInput(node, input, path);
        } else if (input.getVariableSource().equals(EXPRESSION.toString())) {
            validateExpressionInput(node, input, path, startingPayload, nodePayload);
        } else if (input.getVariableSource().equals(SCRIPT.toString())) {
            validateScriptInput(node, input, path, startingPayload, nodePayload);
        } else if (input.getVariableSource().equals(NODE.toString())) {
            validateNodeInput(node, input, path, nodePayload);
        }
    }

    private Set<BpmnComponent.Variable> getGeneratedOutputsByElementType(String elementType) {
        return new HashSet<>(componentLibrary.getAutoGeneratedOutputs().getOrDefault(elementType, Set.of()));
    }
}
