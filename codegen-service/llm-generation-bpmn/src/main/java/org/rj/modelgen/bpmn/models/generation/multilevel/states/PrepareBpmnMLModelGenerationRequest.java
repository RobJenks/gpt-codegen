package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.ArrayList;
import java.util.List;

public class PrepareBpmnMLModelGenerationRequest<TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary> {

    public PrepareBpmnMLModelGenerationRequest(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, ?, ?, ?> params) {
        this(params.getConfig().getModelSchema(), params.getContextProvider(), params.getComponentLibrary(),
                params.getConfig().getComponentLibrarySelector(), params.getConfig().getComponentLibrarySerializer(),
                params.getPromptGenerator(), params.getSelectedPrompt());
    }

    protected PrepareBpmnMLModelGenerationRequest(ModelSchema modelSchema, ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                                 ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector, ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer,
                                                 PromptGenerator<?> promptGenerator, StringSerializable selectedPrompt) {
        super(modelSchema, contextProvider, componentLibrary, componentLibrarySelector, componentLibrarySerializer, promptGenerator, selectedPrompt);
    }

    @Override
    protected List<PromptSubstitution> generateAdditionalPromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        final var substitutions = new ArrayList<>(
                super.generateAdditionalPromptSubstitutions(modelSchema, context, request));

        // Additional substitutions used by all levels of BPMN model generation

        return substitutions;
    }

}
