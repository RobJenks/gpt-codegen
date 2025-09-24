package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;

import java.util.Optional;
import java.util.function.Function;

public class PrepareAndSubmitMLRequestForLevel<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                               TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                               TSubmitImpl extends SubmitGenerationRequestToLlm> extends PrepareAndSubmitLlmGenerationRequest {
    public PrepareAndSubmitMLRequestForLevel(PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params) {
        this(PrepareAndSubmitMLRequestForLevel.class, params);
    }

    public PrepareAndSubmitMLRequestForLevel(Class<? extends PrepareAndSubmitLlmGenerationRequest> cls,
                                             PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params) {
        this(PrepareAndSubmitMLRequestForLevel.class,
             params,
             PrepareAndSubmitMLRequestForLevel::defaultPrepareImpl,
             PrepareAndSubmitMLRequestForLevel::defaultSubmitImpl
        );
    }

    protected PrepareAndSubmitMLRequestForLevel(Class<? extends PrepareAndSubmitLlmGenerationRequest> cls,
                                                PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params,
                                                Function<PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, ?, ?>,
                                                        ? extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>> defaultPrepareImplFactory,
                                                Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>,
                                                        ? extends SubmitGenerationRequestToLlm> defaultSubmitImplFactory) {
        super(PrepareAndSubmitMLRequestForLevel.class,
                createPrepareImpl(params, defaultPrepareImplFactory),
                createSubmitImpl(params, defaultSubmitImplFactory)
        );
    }

        @Override
    public String getSuccessSignalId() {
        return StandardSignals.SUCCESS;
    }

    private static <TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                    TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>>
        PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary> createPrepareImpl(PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, ?, ?> params,
                                                                                                       Function<PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, ?, ?>,
                                                                                                               ? extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>> defaultPrepareImplFactory) {

        return Optional.ofNullable(params.getConfig().getCustomPrepareImplementation())
                .map(generator -> (PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>) generator.apply(params))
                .orElseGet(() -> defaultPrepareImplFactory.apply(params));
    }

    private static <TComponentLibrary extends ComponentLibrary<?>, TSubmitImpl extends SubmitGenerationRequestToLlm>
        SubmitGenerationRequestToLlm createSubmitImpl(PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?> params,
                                                      Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>,
                                                              ? extends SubmitGenerationRequestToLlm> defaultSubmitImplFactory) {

        return Optional.ofNullable(params.getConfig().getCustomSubmitImplementation())
                .map(generator -> (SubmitGenerationRequestToLlm) generator.apply(params))
                .orElseGet(() -> defaultSubmitImplFactory.apply(params));
    }

    protected static <TComponentLibrary extends ComponentLibrary<?>, TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>>
        PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary> defaultPrepareImpl(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?> params) {

        return new PrepareSpecificModelGenerationRequestPromptWithComponents<>(params.getConfig().getModelSchema(), params.getContextProvider(), params.getComponentLibrary(),
                params.getConfig().getComponentLibrarySelector(), params.getConfig().getComponentLibrarySerializer(), params.getPromptGenerator(), params.getSelectedPrompt());
    }

    protected static <TSubmitImpl extends SubmitGenerationRequestToLlm>
        SubmitGenerationRequestToLlm defaultSubmitImpl(PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?> params) {

        return new SubmitGenerationRequestToLlm(params.getConfig().getModelSanitizer());
    }
}
