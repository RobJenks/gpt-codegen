package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.statemodel.states.common.SubmitDetailLevelGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;

public class PrepareAndSubmitMLRequestForDetailLevelParams<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                                     TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                                     TSubmitImpl extends SubmitDetailLevelGenerationRequestToLlm,
                                                     TValidateImpl extends ValidateLlmIntermediateModelResponse>

        extends PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, TValidateImpl> {

    public PrepareAndSubmitMLRequestForDetailLevelParams(MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl, TValidateImpl> config,
                                                         ContextProvider contextProvider, MultiLevelGenerationModelPromptGenerator promptGenerator, MultiLevelModelPromptType selectedPrompt,
                                                         TComponentLibrary componentLibrary) {
        super(config, contextProvider, promptGenerator, selectedPrompt, componentLibrary);
    }
}
