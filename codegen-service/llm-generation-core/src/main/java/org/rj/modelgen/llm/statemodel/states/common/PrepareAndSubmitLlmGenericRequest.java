package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.component.*;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.util.StringSerializable;

public class PrepareAndSubmitLlmGenericRequest<TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareAndSubmitLlmGenerationRequest {

    public PrepareAndSubmitLlmGenericRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType,
                                             TComponentLibrary componentLibrary) {
        this(contextProvider, promptGenerator, promptType, componentLibrary,
                new DefaultComponentLibrarySelector<>(),
                new DefaultComponentLibrarySerializer<>());
    }

    public PrepareAndSubmitLlmGenericRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType,
                                             TComponentLibrary componentLibrary, ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                             ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        super(PrepareAndSubmitLlmGenericRequest.class,
                buildPreparePhase(contextProvider, promptGenerator, promptType, componentLibrary, componentLibrarySelector, componentLibrarySerializer),
                buildSubmissionPhase());
    }

    private static <TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>>
    PrepareGenericModelRequest<TPromptGenerator, TComponentLibrary> buildPreparePhase(ContextProvider contextProvider, TPromptGenerator promptGenerator,
                                                                                      StringSerializable promptType, TComponentLibrary componentLibrary,
                                                                                      ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                                                                      ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        return new PrepareGenericModelRequest<>(contextProvider, promptGenerator, promptType, componentLibrary, componentLibrarySelector, componentLibrarySerializer);
    }

    private static SubmitGenericRequestToLlm buildSubmissionPhase() {
        return new SubmitGenericRequestToLlm();
    }
}
