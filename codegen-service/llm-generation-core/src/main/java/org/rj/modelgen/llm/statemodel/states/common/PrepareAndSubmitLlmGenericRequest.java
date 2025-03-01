package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.util.StringSerializable;

public class PrepareAndSubmitLlmGenericRequest<TSelector, TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitLlmGenericRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType) {
        super(PrepareAndSubmitLlmGenericRequest.class, buildPreparePhase(contextProvider, promptGenerator, promptType), buildSubmissionPhase());
    }

    private static <TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>>
    PrepareGenericModelRequest<TPromptGenerator> buildPreparePhase(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType) {
        return new PrepareGenericModelRequest<>(contextProvider, promptGenerator, promptType);
    }

    private static SubmitGenericRequestToLlm buildSubmissionPhase() {
        return new SubmitGenericRequestToLlm();
    }
}
