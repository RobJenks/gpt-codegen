package org.rj.modelgen.bpmn.models.generation.base.states;

import org.rj.modelgen.bpmn.models.generation.base.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;

public class PrepareAndSubmitBpmnModelGenerationRequestToLlm extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitBpmnModelGenerationRequestToLlm(ModelSchema modelSchema, BpmnGenerationPromptGenerator promptGenerator) {
        super(PrepareAndSubmitBpmnModelGenerationRequestToLlm.class,
                new PrepareBpmnModelGenerationRequest(modelSchema, promptGenerator),
                new SubmitBpmnGenerationRequestToLlm());
    }

    @Override
    public String getDescription() {
        return "Prepare and submit BPMN model generation request to LLM";
    }

    @Override
    public String getSuccessSignalId() {
        return new SubmitBpmnGenerationRequestToLlm().getSuccessSignalId();
    }
}
