package org.rj.modelgen.bpmn.component.synthetic.config;

import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementNode;
import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementType;
import org.rj.modelgen.bpmn.component.synthetic.types.BpmnSyntheticUnknownElementNode;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.synthetic.SyntheticNodeConfig;

import java.util.Map;

public class BpmnSyntheticElementConfig extends SyntheticNodeConfig<String, String, ElementConnection, ElementNode, BpmnIntermediateModel,
        BpmnSyntheticElementType, BpmnSyntheticElementNode, BpmnSyntheticElementConfig> {

    public BpmnSyntheticElementConfig() {
        super();
    }

    public BpmnSyntheticElementConfig(Map<BpmnSyntheticElementType, Class<? extends BpmnSyntheticElementNode>> syntheticNodes) {
        super(syntheticNodes);
    }

    public static BpmnSyntheticElementConfig defaultConfig() {
        return new BpmnSyntheticElementConfig()
                .with(BpmnSyntheticElementType.UnknownElement, BpmnSyntheticUnknownElementNode.class);
    }

}
