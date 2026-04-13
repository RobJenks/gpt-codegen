package org.rj.modelgen.bpmn.intrep.model;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmnReverseRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(BpmnReverseRenderer.class);
    private final BpmnModelInstance inputModel;

    public BpmnReverseRenderer(BpmnModelInstance inputModel) {
        this.inputModel = inputModel;
        if(inputModel == null) throw new IllegalArgumentException("Null input model");
    }

    public BpmnIntermediateModel generateBpmnIntermediateModel() {
        LOG.info("Starting generation of BPMN Intermediate Model");

        BpmnIntermediateModel intermediateModel = new BpmnIntermediateModel();

        // a simple bpmn reverse render to create a map of nodes
        inputModel.getModelElementsByType(FlowNode.class)
                .forEach(node -> {

                    ElementNode elementNode = new ElementNode(node.getId(), node.getName(), node.getElementType().getTypeName());

                    if(node.getOutgoing() != null) {
                        elementNode.setConnectedTo(
                                node.getOutgoing().stream().map(outgoing -> new ElementConnection(outgoing.getTarget().getName(), null)).toList());
                    }

                    intermediateModel.addNode(elementNode);
                });

        return intermediateModel;
    }
   }