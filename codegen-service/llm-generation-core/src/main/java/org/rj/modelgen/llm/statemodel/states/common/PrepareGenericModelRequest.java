package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.component.*;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.List;
import java.util.Optional;

public class PrepareGenericModelRequest<TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareModelGenerationRequest {

    private final TPromptGenerator promptGenerator;
    private final StringSerializable promptType;
    private final TComponentLibrary componentLibrary;
    private final ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector;
    private final ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer;

    public PrepareGenericModelRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType,
                                      TComponentLibrary componentLibrary) {
        this(contextProvider, promptGenerator, promptType, componentLibrary,
                new DefaultComponentLibrarySelector<>(),
                new DefaultComponentLibrarySerializer<>());
    }

    public PrepareGenericModelRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType,
                                      TComponentLibrary componentLibrary, ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                      ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        super(PrepareGenericModelRequest.class, null, contextProvider);
        this.promptGenerator = promptGenerator;
        this.promptType = promptType;
        this.componentLibrary = componentLibrary;
        this.componentLibrarySelector = componentLibrarySelector;
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    @Override
    protected Optional<String> buildGenerationPrompt(ModelSchema modelSchema, Context context, String request, List<PromptSubstitution> substitutions) {
        return promptGenerator.getPrompt(promptType, substitutions);
    }

    @Override
    protected List<PromptSubstitution> generateAdditionalPromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        final var filteredLibrary = componentLibrarySelector.getFilteredLibrary(componentLibrary, getPayload());
        final var serializedLibrary = componentLibrarySerializer.serialize(filteredLibrary);

        return List.of(
                new PromptSubstitution(StandardPromptPlaceholders.COMPONENT_LIBRARY, serializedLibrary)
        );
    }
}
