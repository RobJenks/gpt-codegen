package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.util.StringSerializable;

public class PrepareAndSubmitLlmGenericRequest<TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareAndSubmitLlmGenerationRequest {

    public PrepareAndSubmitLlmGenericRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType, TComponentLibrary componentLibrary) {
        super(PrepareAndSubmitLlmGenericRequest.class, buildPreparePhase(contextProvider, promptGenerator, promptType, componentLibrary), buildSubmissionPhase());
    }

    private static <TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>>
    PrepareGenericModelRequest<TPromptGenerator, TComponentLibrary> buildPreparePhase(ContextProvider contextProvider, TPromptGenerator promptGenerator,
                                                                                      StringSerializable promptType, TComponentLibrary componentLibrary) {
        return new PrepareGenericModelRequest<>(contextProvider, promptGenerator, promptType, componentLibrary);
    }

    private static SubmitGenericRequestToLlm buildSubmissionPhase() {
        return new SubmitGenericRequestToLlm();
    }
}
