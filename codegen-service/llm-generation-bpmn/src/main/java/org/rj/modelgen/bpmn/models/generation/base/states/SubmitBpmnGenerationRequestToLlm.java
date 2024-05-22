package org.rj.modelgen.bpmn.models.generation.base.states;

import org.rj.modelgen.bpmn.intrep.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.base.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;

public class SubmitBpmnGenerationRequestToLlm extends SubmitGenerationRequestToLlm {

    public SubmitBpmnGenerationRequestToLlm() {
        super(SubmitBpmnGenerationRequestToLlm.class, new BpmnIntermediateModelSanitizer());
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
