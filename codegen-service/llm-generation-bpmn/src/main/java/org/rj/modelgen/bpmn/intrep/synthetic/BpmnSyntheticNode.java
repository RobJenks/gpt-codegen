package org.rj.modelgen.bpmn.intrep.synthetic;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.synthetic.SyntheticNode;

public interface BpmnSyntheticNode extends SyntheticNode<String, ElementConnection, ElementNode, BpmnIntermediateModel> { }
