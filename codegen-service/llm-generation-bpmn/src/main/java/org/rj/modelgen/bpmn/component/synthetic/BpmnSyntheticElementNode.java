package org.rj.modelgen.bpmn.component.synthetic;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.synthetic.SyntheticNode;

public interface BpmnSyntheticElementNode extends SyntheticNode<String, String, ElementConnection, ElementNode, BpmnIntermediateModel> {
}
