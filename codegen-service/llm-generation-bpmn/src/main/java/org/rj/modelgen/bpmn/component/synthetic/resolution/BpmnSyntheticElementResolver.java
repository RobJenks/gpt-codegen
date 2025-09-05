package org.rj.modelgen.bpmn.component.synthetic.resolution;

import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementNode;
import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementType;
import org.rj.modelgen.bpmn.component.synthetic.config.BpmnSyntheticElementConfig;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.synthetic.SyntheticNodeResolver;

public class BpmnSyntheticElementResolver extends SyntheticNodeResolver<String, String, ElementConnection, ElementNode, BpmnIntermediateModel,
        BpmnSyntheticElementType, BpmnSyntheticElementNode, BpmnSyntheticElementConfig> {

    public BpmnSyntheticElementResolver(BpmnSyntheticElementConfig config) {
        super(config);
    }
}
