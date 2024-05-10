package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class PrepareAndSubmitMLRequestForLevel<TComponent extends Component> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitMLRequestForLevel(ModelSchema modelSchema, ContextProvider contextProvider, IntermediateModelSanitizer modelSanitizer,
                                             MultiLevelGenerationModelPromptGenerator promptGenerator, MultiLevelModelPromptType selectedPrompt,
                                             ComponentLibrary<TComponent> componentLibrary, ComponentLibrarySerializer<TComponent> componentLibrarySerializer) {
        super(PrepareAndSubmitMLRequestForLevel.class,

                new PrepareSpecificModelGenerationRequestPromptWithComponents<>(modelSchema, contextProvider, componentLibrary,
                        componentLibrarySerializer, promptGenerator, selectedPrompt),

                new SubmitGenerationRequestToLlm(modelSanitizer));
    }

    @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }


}
