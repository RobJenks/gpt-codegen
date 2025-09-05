package org.rj.modelgen.bpmn.component.synthetic.types;

import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementNode;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class BpmnSyntheticUnknownElementNode implements BpmnSyntheticElementNode {

    private static final Logger LOG = LoggerFactory.getLogger(BpmnSyntheticUnknownElementNode.class);
    public static final String NODE_TYPE = "unknownElement";

    public BpmnSyntheticUnknownElementNode() { }

    @Override
    public void resolve(BpmnIntermediateModel model, ElementNode node) {
        final var description = node.getDescription() != null ? node.getDescription() : "(No description available)";
        node.setDescription(description);
        node.setElementType("scriptTask");
    }
}
