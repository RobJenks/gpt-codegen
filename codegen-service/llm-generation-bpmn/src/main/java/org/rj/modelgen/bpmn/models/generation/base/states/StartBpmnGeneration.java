package org.rj.modelgen.bpmn.models.generation.base.states;

import org.rj.modelgen.bpmn.models.generation.base.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.statemodel.states.common.StartGeneration;


public class StartBpmnGeneration extends StartGeneration {
    public StartBpmnGeneration() {
        super(StartBpmnGeneration.class);
    }

    @Override
    public String getDescription() {
        return "Begin BPMN Generation";
    }

    @Override
    public String getSuccessSignalId() {
        return BpmnGenerationSignals.PrepareLlmRequest.toString();
    }
}
