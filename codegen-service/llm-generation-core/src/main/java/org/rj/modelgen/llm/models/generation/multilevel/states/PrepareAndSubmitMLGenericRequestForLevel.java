package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;

import java.util.Optional;

public class PrepareAndSubmitMLGenericRequestForLevel<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                               TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                               TSubmitImpl extends SubmitGenerationRequestToLlm> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitMLGenericRequestForLevel(PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params) {
        super(PrepareAndSubmitMLGenericRequestForLevel.class,
                createPrepareImpl(params),
                createSubmitImpl(params)
        );
    }

    @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }

    private static <TComponentLibrary extends ComponentLibrary<?>, TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>>
        PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary> createPrepareImpl(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?> params) {

        return Optional.ofNullable(params.getConfig().getCustomPrepareImplementation())
                .map(generator -> (PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>) generator.apply(params))
                .orElseGet(() ->
                        new PrepareSpecificModelGenerationRequestPromptWithComponents<>(params.getConfig().getModelSchema(), params.getContextProvider(), params.getComponentLibrary(),
                                params.getConfig().getComponentLibrarySelector(), params.getConfig().getComponentLibrarySerializer(), params.getPromptGenerator(), params.getSelectedPrompt()) );
    }

    private static <TSubmitImpl extends SubmitGenerationRequestToLlm>
        SubmitGenerationRequestToLlm createSubmitImpl(PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?> params) {

        return Optional.ofNullable(params.getConfig().getCustomSubmitImplementation())
                .map(generator -> (SubmitGenerationRequestToLlm) generator.apply(params))
                .orElseGet(() ->
                        new SubmitGenerationRequestToLlm(params.getConfig().getModelSanitizer()));
    }
}
