package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.intmodel.bpmn.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class SubmitBpmnGenerationRequestToLlm extends SubmitGenerationRequestToLlm {

    public SubmitBpmnGenerationRequestToLlm() {
        this(new BpmnIntermediateModelSanitizer());
    }

    public SubmitBpmnGenerationRequestToLlm(IntermediateModelSanitizer modelSanitizer) {
        super(SubmitBpmnGenerationRequestToLlm.class, modelSanitizer);
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
