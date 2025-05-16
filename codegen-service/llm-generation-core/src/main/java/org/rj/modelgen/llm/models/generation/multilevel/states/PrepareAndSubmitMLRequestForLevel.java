package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.validation.impl.IntermediateModelSanitizer;

import java.util.Optional;
import java.util.function.Function;

public class PrepareAndSubmitMLRequestForLevel<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                               TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                               TSubmitImpl extends SubmitGenerationRequestToLlm> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitMLRequestForLevel(MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> config,
                                             ContextProvider contextProvider, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                             MultiLevelModelPromptType selectedPrompt, TComponentLibrary componentLibrary) {
        super(PrepareAndSubmitMLRequestForLevel.class,

                // Prepare phase
                createImpl(config.getCustomPrepareImplementation(),
                        new PrepareSpecificModelGenerationRequestPromptWithComponents<>(config.getModelSchema(), contextProvider, componentLibrary,
                                config.getComponentLibrarySelector(), config.getComponentLibrarySerializer(), promptGenerator, selectedPrompt) ),

                // Submit phase
                createImpl(config.getCustomSubmitImplementation(),
                        new SubmitGenerationRequestToLlm(config.getModelSanitizer()) )
        );
    }

    @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }

    private static <TBase, TImpl extends TBase> TBase createImpl(Function<TBase, TImpl> generator, TBase base) {
        return Optional.ofNullable(generator)
                .map(gen -> (TBase)gen.apply(base))
                .orElse(base);
    }

}
