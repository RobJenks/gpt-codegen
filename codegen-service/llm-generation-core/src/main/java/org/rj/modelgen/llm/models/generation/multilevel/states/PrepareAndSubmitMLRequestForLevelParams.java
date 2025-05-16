package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;

public class PrepareAndSubmitMLRequestForLevelParams<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                                     TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                                     TSubmitImpl extends SubmitGenerationRequestToLlm> {

    private final MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> config;
    private final ContextProvider contextProvider;
    private final MultiLevelGenerationModelPromptGenerator promptGenerator;
    private final MultiLevelModelPromptType selectedPrompt;
    private final TComponentLibrary componentLibrary;

    public PrepareAndSubmitMLRequestForLevelParams(MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> config,
                                                   ContextProvider contextProvider, MultiLevelGenerationModelPromptGenerator promptGenerator, MultiLevelModelPromptType selectedPrompt,
                                                   TComponentLibrary componentLibrary) {
        this.config = config;
        this.contextProvider = contextProvider;
        this.promptGenerator = promptGenerator;
        this.selectedPrompt = selectedPrompt;
        this.componentLibrary = componentLibrary;
    }

    public MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, TPrepareImpl, TSubmitImpl> getConfig() {
        return config;
    }

    public ContextProvider getContextProvider() {
        return contextProvider;
    }

    public MultiLevelGenerationModelPromptGenerator getPromptGenerator() {
        return promptGenerator;
    }

    public MultiLevelModelPromptType getSelectedPrompt() {
        return selectedPrompt;
    }

    public TComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }
}
