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
    public PrepareAndSubmitMLRequestForLevel(PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> params) {
        super(PrepareAndSubmitMLRequestForLevel.class,
                createPrepareImpl(params),
                createSubmitImpl(params)
        );
    }

    @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }

    private static <TComponentLibrary extends ComponentLibrary<?>, TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>>
        PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary> createPrepareImpl(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?> params) {

        return Optional.ofNullable(params.getConfig().getCustomPrepareImplementation())
                .map(generator -> (PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>) generator.apply(params))
                .orElseGet(() ->
                        new PrepareSpecificModelGenerationRequestPromptWithComponents<>(params.getConfig().getModelSchema(), params.getContextProvider(), params.getComponentLibrary(),
                                params.getConfig().getComponentLibrarySelector(), params.getConfig().getComponentLibrarySerializer(), params.getPromptGenerator(), params.getSelectedPrompt()) );
    }

    private static <TSubmitImpl extends SubmitGenerationRequestToLlm>
        SubmitGenerationRequestToLlm createSubmitImpl(PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl> params) {

        return Optional.ofNullable(params.getConfig().getCustomSubmitImplementation())
                .map(generator -> (SubmitGenerationRequestToLlm) generator.apply(params))
                .orElseGet(() ->
                        new SubmitGenerationRequestToLlm(params.getConfig().getModelSanitizer()));
    }
}
