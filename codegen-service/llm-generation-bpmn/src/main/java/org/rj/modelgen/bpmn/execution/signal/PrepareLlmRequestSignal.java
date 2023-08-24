package org.rj.modelgen.bpmn.execution.signal;

import org.rj.modelgen.bpmn.execution.state.PrepareBpmnModelGenerationRequest;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class PrepareLlmRequestSignal extends ModelInterfaceSignal<PrepareBpmnModelGenerationRequest> {
    public static final String ID = PrepareLlmRequestSignal.class.getName();
    public PrepareLlmRequestSignal() {
        super(ID);
    }

    @Override
    public String getDescription() {
        return "Prepare new LLM request for submission";
    }
}
