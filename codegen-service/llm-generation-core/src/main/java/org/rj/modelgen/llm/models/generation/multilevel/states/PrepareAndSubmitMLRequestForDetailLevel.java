package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenerationRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitDetailLevelGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;


public class PrepareAndSubmitMLRequestForDetailLevel<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                                     TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                                     TSubmitImpl extends SubmitDetailLevelGenerationRequestToLlm>

        extends PrepareAndSubmitMLRequestForLevel<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> {


    public PrepareAndSubmitMLRequestForDetailLevel(PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params) {
        this(PrepareAndSubmitMLRequestForDetailLevel.class, params);
    }

    public PrepareAndSubmitMLRequestForDetailLevel(Class<? extends PrepareAndSubmitLlmGenerationRequest> cls,
                                                   PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, ?> params) {
        super(cls, params,
                PrepareAndSubmitMLRequestForLevel::defaultPrepareImpl,
                PrepareAndSubmitMLRequestForDetailLevel::defaultDetailLevelSubmitImpl
        );
    }

    protected static <TSubmitImpl extends SubmitDetailLevelGenerationRequestToLlm>
    SubmitDetailLevelGenerationRequestToLlm defaultDetailLevelSubmitImpl(PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?> params) {

        return new SubmitDetailLevelGenerationRequestToLlm(params.getConfig().getModelSanitizer());
    }
}
