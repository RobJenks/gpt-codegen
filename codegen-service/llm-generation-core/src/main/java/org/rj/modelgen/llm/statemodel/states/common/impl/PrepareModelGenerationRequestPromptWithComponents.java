package org.rj.modelgen.llm.statemodel.states.common.impl;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.PrepareModelGenerationRequest;

import java.util.List;

public abstract class PrepareModelGenerationRequestPromptWithComponents<TComponentLibrary extends ComponentLibrary<?>> extends PrepareModelGenerationRequest {
    private final TComponentLibrary componentLibrary;
    private final ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector;
    private final ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer;

    public PrepareModelGenerationRequestPromptWithComponents(ModelSchema modelSchema, ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                                             ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                                             ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this(PrepareModelGenerationRequestPromptWithComponents.class, modelSchema, contextProvider, componentLibrary,
                componentLibrarySelector,componentLibrarySerializer);
    }

    public PrepareModelGenerationRequestPromptWithComponents(Class<? extends PrepareModelGenerationRequest> cls, ModelSchema modelSchema, ContextProvider contextProvider,
                                                             TComponentLibrary componentLibrary, ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                                             ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        super(cls, modelSchema, contextProvider);
        this.componentLibrary = componentLibrary;
        this.componentLibrarySelector = componentLibrarySelector;
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    @Override
    protected List<PromptSubstitution> generateAdditionalPromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        final var filteredLibrary = componentLibrarySelector.getFilteredLibrary(componentLibrary, getPayload());
        final var serializedLibrary = componentLibrarySerializer.serialize(filteredLibrary);

        return List.of(
                new PromptSubstitution(StandardPromptPlaceholders.COMPONENT_LIBRARY, serializedLibrary)
        );
    }

    protected TComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }
}
