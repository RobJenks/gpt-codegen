package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;

public class SubmitBpmnGenerationRequestToLlm extends SubmitGenerationRequestToLlm {

    public SubmitBpmnGenerationRequestToLlm() {
        super(SubmitBpmnGenerationRequestToLlm.class);
    }

    @Override
    public String getDescription() {
        return "Submit new BPMN generation request to LLM";
    }

    @Override
    public String getSuccessSignalId() {
        return BpmnGenerationSignals.ValidateLlmResponse.toString();
    }
}
