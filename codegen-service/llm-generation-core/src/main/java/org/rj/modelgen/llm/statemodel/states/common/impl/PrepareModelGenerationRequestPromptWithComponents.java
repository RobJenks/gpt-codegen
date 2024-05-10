package org.rj.modelgen.llm.statemodel.states.common.impl;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.PrepareModelGenerationRequest;

import java.util.List;

public abstract class PrepareModelGenerationRequestPromptWithComponents<TComponent extends Component> extends PrepareModelGenerationRequest {
    private final ComponentLibrary<TComponent> componentLibrary;
    private final ComponentLibrarySerializer<TComponent> componentLibrarySerializer;

    public PrepareModelGenerationRequestPromptWithComponents(ModelSchema modelSchema, ContextProvider contextProvider,
                                                             ComponentLibrary<TComponent> componentLibrary, ComponentLibrarySerializer<TComponent> componentLibrarySerializer) {
        this(PrepareModelGenerationRequestPromptWithComponents.class, modelSchema, contextProvider, componentLibrary, componentLibrarySerializer);
    }

    public PrepareModelGenerationRequestPromptWithComponents(Class<? extends PrepareModelGenerationRequest> cls, ModelSchema modelSchema, ContextProvider contextProvider,
                                                             ComponentLibrary<TComponent> componentLibrary, ComponentLibrarySerializer<TComponent> componentLibrarySerializer) {
        super(cls, modelSchema, contextProvider);
        this.componentLibrary = componentLibrary;
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    @Override
    protected List<PromptSubstitution> generateAdditionalPromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        return List.of(
                new PromptSubstitution(StandardPromptPlaceholders.COMPONENT_LIBRARY, componentLibrarySerializer.serialize(componentLibrary))
        );
    }
}
