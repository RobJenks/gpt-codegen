package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultilevelModelPreprocessingConfig;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevel;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;
import org.rj.modelgen.llm.models.generation.multilevel.states.StartMultiLevelGeneration;
import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenericRequest;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.GenerateModelFromIntermediateModelTransformer;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class MultiLevelGenerationModel<THighLevelModel extends IntermediateModel,
                                                TDetailLevelModel extends IntermediateModel,
                                                TModel,
                                                TComponentLibrary extends ComponentLibrary<?>,
                                                TResult>
                                                extends ModelInterfaceStateMachine {

    public MultiLevelGenerationModel(Class<? extends MultiLevelGenerationModel<THighLevelModel, TDetailLevelModel, TModel, TComponentLibrary, TResult>> modelClass,
                                     ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                     ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                     MultilevelModelPreprocessingConfig<TComponentLibrary> preprocessingConfig,
                                     MultiLevelModelPhaseConfig<THighLevelModel, TComponentLibrary, ?, ?> highLevelPhaseConfig,
                                     MultiLevelModelPhaseConfig<TDetailLevelModel, TComponentLibrary, ?, ?> detailLevelPhaseConfig,
                                     ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
                                     Function<TModel, String> renderedModelSerializer,
                                     ModelInterfaceState completionState,
                                     MultiLevelGenerationModelOptions options) {
        this(modelClass, modelInterface, buildModelData(promptGenerator, contextProvider, componentLibrary, preprocessingConfig, highLevelPhaseConfig,
                detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, completionState, options));
    }

    private MultiLevelGenerationModel(Class<? extends MultiLevelGenerationModel<THighLevelModel, TDetailLevelModel, TModel, TComponentLibrary, TResult>> modelClass,
                                      ModelInterface modelInterface, ModelData modelData) {
        super(modelClass, modelInterface, modelData.getStates(), modelData.getRules());
    }


    private static<THighLevelModel extends IntermediateModel,
                   TDetailLevelModel extends IntermediateModel,
                   TModel,
                   TComponentLibrary extends ComponentLibrary<?>>
    ModelData buildModelData(
            MultiLevelGenerationModelPromptGenerator promptGenerator,
            ContextProvider contextProvider, TComponentLibrary componentLibrary,
            MultilevelModelPreprocessingConfig<TComponentLibrary> preprocessingConfig,
            MultiLevelModelPhaseConfig<THighLevelModel, TComponentLibrary, ?, ?> highLevelPhaseConfig,
            MultiLevelModelPhaseConfig<TDetailLevelModel, TComponentLibrary, ?, ?> detailLevelPhaseConfig,
            ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
            Function<TModel, String> renderedModelSerializer,
            ModelInterfaceState completionState,
            MultiLevelGenerationModelOptions options) {

        // Overrides from model options
        final var modelOptions = Optional.ofNullable(options).orElseGet(MultiLevelGenerationModelOptions::defaultOptions);
        final var modelPromptGenerator = modelOptions.applyPromptGeneratorCustomization(promptGenerator);

        Optional.ofNullable(modelOptions.getHighLevelSchemaOverride()).ifPresent(highLevelPhaseConfig::setModelSchema);
        Optional.ofNullable(modelOptions.getDetailLevelSchemaOverride()).ifPresent(detailLevelPhaseConfig::setModelSchema);

        // Build each model state
        final var stateInit = new StartMultiLevelGeneration()
                .withOverriddenId(MultiLevelGenerationModelStates.StartMultiLevelGeneration);

        final var stateSanitizingPrePass = new PrepareAndSubmitLlmGenericRequest<>(contextProvider, promptGenerator, MultiLevelModelPromptType.SanitizingPrePass, componentLibrary)
                .withResponseOutputKey(StandardModelData.Request)
                .withOverriddenId(MultiLevelGenerationModelStates.SanitizingPrePass);

        final var statePreprocessing = new PrepareAndSubmitLlmGenericRequest<>(contextProvider, promptGenerator, MultiLevelModelPromptType.PreProcessing,
                componentLibrary, preprocessingConfig.getComponentLibrarySelector(), preprocessingConfig.getComponentLibrarySerializer())
                .withResponseOutputKey(StandardModelData.Request)
                .withOverriddenId(MultiLevelGenerationModelStates.PreProcessing);

        final var stateExecuteHighLevel = new PrepareAndSubmitMLRequestForLevel<>(
                new PrepareAndSubmitMLRequestForLevelParams<>(highLevelPhaseConfig, contextProvider, modelPromptGenerator, MultiLevelModelPromptType.GenerateHighLevel, componentLibrary))
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ExecuteHighLevel);

        final var stateValidateHighLevel = new ValidateLlmIntermediateModelResponse(
                    highLevelPhaseConfig.getModelSchema(), highLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ValidateHighLevel);

        final var stateExecuteDetailLevel = new PrepareAndSubmitMLRequestForLevel<>(
                new PrepareAndSubmitMLRequestForLevelParams<>(detailLevelPhaseConfig, contextProvider, modelPromptGenerator, MultiLevelModelPromptType.GenerateDetailLevel, componentLibrary))
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ExecuteDetailLevel);

        //final var stateReturnToHighLevelIfRequired = new { ... } // TODO

        final var stateValidateDetailLevel = new ValidateLlmIntermediateModelResponse(
                    detailLevelPhaseConfig.getModelSchema(), detailLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ValidateDetailLevel);

        final var stateGenerateModel = new GenerateModelFromIntermediateModelTransformer<>(
                    GenerateModelFromIntermediateModelTransformer.class,
                    detailLevelPhaseConfig.getIntermediateModelClass(),
                    MultiLevelModelStandardPayloadData.DetailLevelModel.toString(), // Input
                    StandardModelData.GeneratedModel.toString(),                    // Output
                    modelGenerationFunction, renderedModelSerializer)
                .withOverriddenId(MultiLevelGenerationModelStates.GenerateModel);

        // final var stateValidateModelCorrectness = new { ... } // TODO
        final var stateComplete = completionState
                .withOverriddenId(MultiLevelGenerationModelStates.Complete);

        final var states = List.of(stateInit, stateSanitizingPrePass, statePreprocessing, stateExecuteHighLevel, stateValidateHighLevel,
                                   stateExecuteDetailLevel, stateValidateDetailLevel, stateGenerateModel, stateComplete);

        // Complete initialization, and apply any global model state that the states want to consume
        states.forEach(ModelInterfaceState::completeStateInitialization);
        states.forEach(state -> state.applyModelOptions(options));

        // Transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule(stateInit, StandardSignals.SUCCESS, stateSanitizingPrePass),

                new ModelInterfaceTransitionRule(stateSanitizingPrePass, StandardSignals.SUCCESS, statePreprocessing),
                new ModelInterfaceTransitionRule(stateSanitizingPrePass, StandardSignals.SKIPPED, statePreprocessing),  // Optional stage

                new ModelInterfaceTransitionRule(statePreprocessing, StandardSignals.SUCCESS, stateExecuteHighLevel),
                new ModelInterfaceTransitionRule(statePreprocessing, StandardSignals.SKIPPED, stateExecuteHighLevel),  // Optional stage

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
     * @param data          Initial input data to be passed into the model in the input signal
     * @return              Model execution result
     */
    public abstract Mono<TResult> executeModel(String sessionId, String request, Map<String, Object> data);

    protected Class<? extends ModelInterfaceState> getInitialState() {
        return StartMultiLevelGeneration.class;
    }

    protected MultiLevelModelStandardSignals getStartSignal() {
        return MultiLevelModelStandardSignals.StartGeneration;
    }


}
