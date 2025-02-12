package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;

public class PrepareAndSubmitLlmGenericRequest<TSelector, TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator, TSelector>> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitLlmGenericRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, TSelector promptType) {
        super(PrepareAndSubmitLlmGenericRequest.class, buildPreparePhase(contextProvider, promptGenerator, promptType), buildSubmissionPhase());
    }

    private static <TSelector, TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator, TSelector>>
    PrepareGenericModelRequest<TSelector, TPromptGenerator> buildPreparePhase(ContextProvider contextProvider, TPromptGenerator promptGenerator, TSelector promptType) {
        return new PrepareGenericModelRequest<>(contextProvider, promptGenerator, promptType);
    }

    private static SubmitGenericRequestToLlm buildSubmissionPhase() {
        return new SubmitGenericRequestToLlm();
    }
}
