package org.rj.modelgen.bpmn.models.generation.validation;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.*;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.intrep.model.*;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;

import java.util.*;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.*;
import static org.rj.modelgen.bpmn.generation.BpmnModelComponentNodeScriptVariableType.*;
import static org.rj.modelgen.llm.util.ValidationUtils.identifyNumberOfRoots;
import static org.rj.modelgen.llm.util.ValidationUtils.convertStringToMap;

public class ValidateBpmnModel {

    private static final String FULL_PROCESS = "full_process";

    private BpmnIntermediateModel model;
    private BpmnGlobalVariableLibrary globalVariableLibrary;
    private BpmnComponentLibrary componentLibrary;
    private List<IntermediateModelValidationError> invalidMessages = new ArrayList<>();

    public ValidateBpmnModel(BpmnIntermediateModel model, BpmnGlobalVariableLibrary globalVariableLibrary, BpmnComponentLibrary componentLibrary) {
        this.model = model;
        this.globalVariableLibrary = globalVariableLibrary;
        this.componentLibrary = componentLibrary;
    }

    public List<IntermediateModelValidationError> validate() {
        for (ElementNode node : model.getNodes()) {
            validateNodeNames(node);
            validateRequiredInputs(node);
            validateOutputNames(node);
            validateNodeConnections(node);
            validateNodeConnectionsRules(node);
        }
        identifyOrphanedNodes();

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

        node.getInputs().forEach(nodeInput -> {
            if (StringUtils.isBlank(nodeInput.getName())) {
                invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has input with null or empty name", node.getId()), node.getId()));
            } else {
                if (StringUtils.isBlank(nodeInput.getValue())) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null or empty value", node.getId(), nodeInput.getName()), node.getId()));
                }
                if (StringUtils.isBlank(nodeInput.getVariableSource())) {
                    invalidInputMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has '%s' input with null or empty variableSource. It should be CONSTANT, SCRIPT, NODE or GLOBAL.", node.getId(), nodeInput.getName()), node.getId()));
                }
            }
        });

        if (!invalidInputMessages.isEmpty()) {
            // All inputs must have valid values before proceeding with further validation
            invalidMessages.addAll(invalidInputMessages);
            return;
        }

        component.get().getRequiredInputs().forEach(requiredInput -> {
            var inputs = node.getInputs().stream()
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
            // If the input is enum, it must have a valid value
            if (requiredInput.getAllowedValues() != null && !requiredInput.getAllowedValues().isEmpty() && !requiredInput.getAllowedValues().contains(inputs.get(0).getValue())) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an invalid value for input '%s'. Allowed values are: %s", node.getId(), requiredInput.getName(), String.join(", ", requiredInput.getAllowedValues())), node.getId()));
            }
            if (!inputs.isEmpty() && inputs.get(0).getVariableSource().equals(GLOBAL.toString()) && globalVariableLibrary.getVariableByName(inputs.get(0).getValue()).isEmpty()) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an input '%s' which tries to use a global variable '%s' that does not exist in the global variable library", node.getId(), requiredInput.getName(), inputs.get(0).getValue()), node.getId()));
            }
        });
    }

    private void validateOutputNames(ElementNode node) {
        final var component = componentLibrary.getComponentByName(node.getElementType());
        if (component.isEmpty()) return;   // Unknown component type, should probably never happen by the time we reach this point

        node.getOutputs().stream()
                .flatMap(x -> validateOutputName(x.getName(), component.get()).stream())
                .forEach(msg -> invalidMessages.add(new IntermediateModelValidationError(String.format("Node '%s' has an invalid output: %s", node.getId(), msg), node.getId())));
    }

    private Optional<String> validateOutputName(String name, BpmnComponent component) {
        if (StringUtils.isBlank(name)) return Optional.of("Name cannot be null or blank");

        // No possible conflict if the node does not generate fixed outputs
        if (component.getGeneratedOutputs() == null) return Optional.empty();

        final String outputName = name.trim();
        final var conflict = component.getGeneratedOutputs().stream()
                .anyMatch(x -> outputName.equals(x.getName()));

        if (conflict) {
            return Optional.of(("Cannot add a custom output with name '%s' since the node already generates this output automatically. " +
                    "Either change the output name to something else and adjust other nodes which may use it accordingly or just reference the automatically generated output in other nodes").formatted(name));
        }

        return Optional.empty();
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
                invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' is missing a default target node in its inputs. A default target node must be specified.", node.getId()), node.getId()));
            } else if(node.getConnectedTo().stream().noneMatch(c -> c.getTargetNode().equals(defaultTargetNodeName))) {
                invalidMessages.add(new IntermediateModelValidationError(String.format("Gateway node '%s' has a default target node '%s' which is not among its outgoing connections. The default target node must be one of the outgoing connections.", node.getId(), defaultTargetNodeName), node.getId()));
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
}
