package org.rj.modelgen.llm.statemodel.states.common.impl;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareModelGenerationRequest;

import java.util.List;
import java.util.Optional;

public class PrepareSpecificModelGenerationRequestPromptWithComponents<TPromptSelector, TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareModelGenerationRequestPromptWithComponents<TComponentLibrary> {

    private final PromptGenerator<?, TPromptSelector> promptGenerator;
    private final TPromptSelector selectedPrompt;

    public PrepareSpecificModelGenerationRequestPromptWithComponents(ModelSchema modelSchema, ContextProvider contextProvider,
                                                                     TComponentLibrary componentLibrary, ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                                                     ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer,
                                                                     PromptGenerator<?, TPromptSelector> promptGenerator, TPromptSelector selectedPrompt) {
        this(PrepareSpecificModelGenerationRequestPromptWithComponents.class, modelSchema, contextProvider, componentLibrary,
                componentLibrarySelector, componentLibrarySerializer, promptGenerator, selectedPrompt);
    }
    public PrepareSpecificModelGenerationRequestPromptWithComponents(Class<? extends PrepareModelGenerationRequest> cls, ModelSchema modelSchema, ContextProvider contextProvider,
                                                                     TComponentLibrary componentLibrary, ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                                                     ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer,
                                                                     PromptGenerator<?, TPromptSelector> promptGenerator, TPromptSelector selectedPrompt) {
        super(cls, modelSchema, contextProvider, componentLibrary,componentLibrarySelector ,componentLibrarySerializer);
        this.promptGenerator = promptGenerator;
        this.selectedPrompt = selectedPrompt;
    }

    @Override
    protected Optional<String> buildGenerationPrompt(ModelSchema modelSchema, Context context, String request, List<PromptSubstitution> substitutions) {
        return promptGenerator.getPrompt(selectedPrompt, substitutions);
    }

    @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }
}
