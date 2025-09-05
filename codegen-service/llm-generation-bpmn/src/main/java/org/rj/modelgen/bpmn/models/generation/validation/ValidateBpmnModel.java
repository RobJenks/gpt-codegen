package org.rj.modelgen.bpmn.models.generation.validation;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;

import java.util.ArrayList;
import java.util.List;

import static org.rj.modelgen.llm.util.ValidationUtils.identifyNumberOfRoots;

public class ValidateBpmnModel {

    private static final String FULL_PROCESS = "full_process";
    private BpmnIntermediateModel model;
    private BpmnComponentLibrary componentLibrary;
    private List<IntermediateModelValidationError> invalidMessages = new ArrayList<>();


    public ValidateBpmnModel(BpmnIntermediateModel model, BpmnComponentLibrary componentLibrary) {
        this.model = model;
        this.componentLibrary = componentLibrary;
    }

    public List<IntermediateModelValidationError> validate() {
        for (ElementNode node : model.getNodes()) {
            validateNodeNames(node);
            validateNodeConnections(node);
        }
        identifyOrphanedNodes();
        validateNodeConnectionsRules();
        return invalidMessages;
    }

    private void validateNodeNames(ElementNode node) {
        if (StringUtils.isBlank(node.getName())) {
            invalidMessages.add(new IntermediateModelValidationError("Node name cannot be null or blank", FULL_PROCESS));
        }
        if (StringUtils.isBlank(node.getElementType())) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Node type cannot be null or blank for node '%s' ", node.getName()), node.getName()));
        }
        var component = componentLibrary.getComponentByName(node.getElementType());
        if (component.isEmpty()) {
            invalidMessages.add(new IntermediateModelValidationError(String.format("Component '%s' is not found in library for node %s. Select a valid component.", node.getElementType(), node.getName()), node.getName()));
        }
    }

    private void validateNodeConnections(ElementNode node) {
        if (node.getConnectedTo() != null) {
            node.getConnectedTo().forEach(connection -> {
                if (StringUtils.isBlank(connection.getTargetNode())) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Connection for node '%s' has no target node defined", node.getName()), node.getName()));
                } else if (model.getNodes().stream().noneMatch(n -> n.getId().equals(connection.getTargetNode()))) {
                    invalidMessages.add(new IntermediateModelValidationError(String.format("Target node '%s' does not exist in the model, but the connection of node '%s' tries to point to this non-existent node", connection.getTargetNode(), node.getName()), node.getName()));
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

    private void validateNodeConnectionsRules() {
        // TODO: Check connection between node types e.g. most node types can have only one connection between them, gateways can have multiple connections, etc.
    }


}
