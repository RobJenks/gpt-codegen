package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevel;
import org.rj.modelgen.llm.models.generation.multilevel.states.StartMultiLevelGeneration;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.GenerateModelFromIntermediateModelTransformer;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class MultiLevelGenerationModel<THighLevelModel extends IntermediateModel,
                                                TDetailLevelModel extends IntermediateModel,
                                                TModel,
                                                TComponentLibrary extends ComponentLibrary<?>,
                                                TResult> extends ModelInterfaceStateMachine {

    public MultiLevelGenerationModel(ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                     ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                     MultiLevelModelPhaseConfig<THighLevelModel, TComponentLibrary> highLevelPhaseConfig,
                                     MultiLevelModelPhaseConfig<TDetailLevelModel, TComponentLibrary> detailLevelPhaseConfig,
                                     ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
                                     ModelInterfaceState completionState) {
        this(modelInterface, buildModelData(promptGenerator, contextProvider, componentLibrary, highLevelPhaseConfig,
                detailLevelPhaseConfig, modelGenerationFunction, completionState));
    }

    private MultiLevelGenerationModel(ModelInterface modelInterface, ModelData modelData) {
        super(modelInterface, modelData.getStates(), modelData.getRules());
    }


    private static<THighLevelModel extends IntermediateModel,
                   TDetailLevelModel extends IntermediateModel,
                   TModel,
                   TComponentLibrary extends ComponentLibrary<?>> ModelData buildModelData(
            MultiLevelGenerationModelPromptGenerator promptGenerator,
            ContextProvider contextProvider, TComponentLibrary componentLibrary,
            MultiLevelModelPhaseConfig<THighLevelModel, TComponentLibrary> highLevelPhaseConfig,
            MultiLevelModelPhaseConfig<TDetailLevelModel, TComponentLibrary> detailLevelPhaseConfig,
            ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
            ModelInterfaceState completionState) {

        final var stateInit = new StartMultiLevelGeneration();

        final var stateExecuteHighLevel = new PrepareAndSubmitMLRequestForLevel<>(highLevelPhaseConfig.getModelSchema(), contextProvider,
                highLevelPhaseConfig.getModelSanitizer(), promptGenerator, MultiLevelModelPromptType.GenerateHighLevel,
                componentLibrary, highLevelPhaseConfig.getComponentLibrarySerializer())
                //.withOverriddenModelSuccessResponse(Util.loadStringResource("example-1-input.json"))
                .withOverriddenId("executeHighLevel");

        final var stateValidateHighLevel = new ValidateLlmIntermediateModelResponse(
                highLevelPhaseConfig.getModelSchema(), highLevelPhaseConfig.getIntermediateModelClass())
                .withOverriddenId("validateHighLevel");

        final var stateExecuteDetailLevel = new PrepareAndSubmitMLRequestForLevel<>(detailLevelPhaseConfig.getModelSchema(), contextProvider,
                detailLevelPhaseConfig.getModelSanitizer(), promptGenerator, MultiLevelModelPromptType.GenerateDetailLevel,
                componentLibrary, detailLevelPhaseConfig.getComponentLibrarySerializer())
                .withOverriddenModelSuccessResponse("Here is result 2")
                .withOverriddenId("executeDetailLevel");
        //final var stateReturnToHighLevelIfRequired = new { ... } // TODO
        final var stateValidateDetailLevel = new ValidateLlmIntermediateModelResponse(
                detailLevelPhaseConfig.getModelSchema(), detailLevelPhaseConfig.getIntermediateModelClass())
                .withOverriddenId("validateDetailLevel");

        final var stateGenerateModel = new GenerateModelFromIntermediateModelTransformer<>(
                GenerateModelFromIntermediateModelTransformer.class,
                detailLevelPhaseConfig.getIntermediateModelClass(), StandardModelData.SanitizedContent.toString(),
                StandardModelData.GeneratedModel.toString(), modelGenerationFunction);

        // final var stateValidateModelCorrectness = new { ... } // TODO
        final var stateComplete = completionState;

        final var states = List.of(stateInit, stateExecuteHighLevel, stateValidateHighLevel, stateExecuteDetailLevel,
                stateValidateDetailLevel, stateGenerateModel, stateComplete);

        // Transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule(stateInit, StandardSignals.SUCCESS, stateExecuteHighLevel),
                new ModelInterfaceTransitionRule(stateExecuteHighLevel, StandardSignals.SUCCESS, stateValidateHighLevel),
                new ModelInterfaceTransitionRule(stateValidateHighLevel, StandardSignals.SUCCESS, stateExecuteDetailLevel),
                new ModelInterfaceTransitionRule(stateExecuteDetailLevel, StandardSignals.SUCCESS, stateValidateDetailLevel),
                new ModelInterfaceTransitionRule(stateValidateDetailLevel, StandardSignals.SUCCESS, stateGenerateModel),
                new ModelInterfaceTransitionRule(stateGenerateModel, StandardSignals.SUCCESS, stateComplete)
        ));

        return new ModelData(states, rules);
    }

    /**
     * Implemented by subclasses to trigger execution of a specific model type
     *
     * @param sessionId     Current session
     * @param request       Input prompt
     * @return              Model execution result
     */
    public abstract Mono<TResult> executeModel(String sessionId, String request);

    protected Class<? extends ModelInterfaceState> getInitialState() {
        return StartMultiLevelGeneration.class;
    }

    protected MultiLevelModelStandardSignals getStartSignal() {
        return MultiLevelModelStandardSignals.StartGeneration;
    }

    private static class ModelData {
        private final List<ModelInterfaceState> states;
        private final ModelInterfaceTransitionRules rules;

        public ModelData(List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
            this.states = states;
            this.rules = rules;
        }

        public List<ModelInterfaceState> getStates() {
            return states;
        }

        public ModelInterfaceTransitionRules getRules() {
            return rules;
        }
    }

}
