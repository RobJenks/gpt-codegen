package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.intrep.ModelParser;
import org.rj.modelgen.llm.intrep.assets.IntermediateModelAssets;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.GenerationModel;
import org.rj.modelgen.llm.models.generation.GenerationResult;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelDetailPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultilevelModelPreprocessingConfig;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.models.generation.multilevel.states.*;
import org.rj.modelgen.llm.models.generation.multilevel.states.ReverseRenderIntermediateModel;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenericRequest;
import org.rj.modelgen.llm.statemodel.states.common.impl.GenerateModelFromIntermediateModelTransformer;
import org.rj.modelgen.llm.subproblem.config.SubproblemDecompositionConfig;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionSignals;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals.UsePreprocessingFlow;
import static org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals.UseReverseRenderingFlow;

public abstract class MultiLevelGenerationModel<THighLevelModel extends IntermediateModel,
                                                TDetailLevelModel extends IntermediateModel,
                                                TIntermediateModelAssets extends IntermediateModelAssets,
                                                TModel,
                                                TComponentLibrary extends ComponentLibrary<?>,
                                                TResult extends GenerationResult>
                                                extends GenerationModel<TResult> {

    public MultiLevelGenerationModel(Class<? extends MultiLevelGenerationModel<THighLevelModel, TDetailLevelModel, TIntermediateModelAssets, TModel, TComponentLibrary, TResult>> modelClass,
                                     ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                     ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                     MultilevelModelPreprocessingConfig<TComponentLibrary> preprocessingConfig,
                                     MultiLevelModelPhaseConfig<THighLevelModel, TIntermediateModelAssets, TComponentLibrary, ?, ?, ?> highLevelPhaseConfig,
                                     ReverseRenderFunction<TModel, TDetailLevelModel> reverseRenderFunction,
                                     MultiLevelModelDetailPhaseConfig<TDetailLevelModel, TIntermediateModelAssets, TComponentLibrary, ?, ?, ?> detailLevelPhaseConfig,
                                     ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
                                     Function<TModel, String> renderedModelSerializer,
                                     SubproblemDecompositionConfig subproblemDecompositionConfig,
                                     SubproblemDecompositionConfig reverseRenderSubproblemDecompositionConfig,
                                     ModelInterfaceState completionState,
                                     MultiLevelGenerationModelOptions options,
                                     ModelParser<TModel> modelParser) {
        this(modelClass, modelInterface, buildModelData(promptGenerator, contextProvider, componentLibrary, preprocessingConfig, highLevelPhaseConfig, reverseRenderFunction,
                detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig, reverseRenderSubproblemDecompositionConfig, completionState, options, modelParser));
    }

    private MultiLevelGenerationModel(Class<? extends MultiLevelGenerationModel<THighLevelModel, TDetailLevelModel, TIntermediateModelAssets, TModel, TComponentLibrary, TResult>> modelClass,
                                      ModelInterface modelInterface, ModelData modelData) {
        super(modelClass, modelInterface, modelData.getStates(), modelData.getRules());
    }


    private static<THighLevelModel extends IntermediateModel,
                   TDetailLevelModel extends IntermediateModel,
                   TIntermediateModelAssets extends IntermediateModelAssets,
                   TModel,
                   TComponentLibrary extends ComponentLibrary<?>>
    ModelData buildModelData(
            MultiLevelGenerationModelPromptGenerator promptGenerator,
            ContextProvider contextProvider, TComponentLibrary componentLibrary,
            MultilevelModelPreprocessingConfig<TComponentLibrary> preprocessingConfig,
            MultiLevelModelPhaseConfig<THighLevelModel, TIntermediateModelAssets, TComponentLibrary, ?, ?, ?> highLevelPhaseConfig,
            ReverseRenderFunction<TModel, TDetailLevelModel> reverseRenderFunction,
            MultiLevelModelDetailPhaseConfig<TDetailLevelModel, TIntermediateModelAssets, TComponentLibrary, ?, ?, ?> detailLevelPhaseConfig,
            ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
            Function<TModel, String> renderedModelSerializer,
            SubproblemDecompositionConfig subproblemDecompositionConfig,
            SubproblemDecompositionConfig reverseRenderSubproblemDecompositionConfig,
            ModelInterfaceState completionState,
            MultiLevelGenerationModelOptions options,
            ModelParser<TModel> modelParser) {

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

        final var stateGenerateSubproblems = subproblemDecompositionConfig.getSubproblemGeneratorImplementation().get()
                .withInputKey(StandardModelData.Request)
                .withOutputKey(StandardModelData.Request)
                .withSubproblemDecompositionEnabled(modelOptions.shouldPerformSubproblemDecomposition())
                .withOverriddenId(MultiLevelGenerationModelStates.GenerateSubproblems);

        final var stateExecuteHighLevel = new PrepareAndSubmitMLRequestForLevel<>(
                new PrepareAndSubmitMLRequestForLevelParams<>(highLevelPhaseConfig, contextProvider, modelPromptGenerator, MultiLevelModelPromptType.GenerateHighLevel, componentLibrary))
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ExecuteHighLevel);

        final var stateValidateHighLevel = highLevelPhaseConfig.createValidationStage(
                    highLevelPhaseConfig.getModelSchema(), highLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ValidateHighLevel);

        final var stateReverseRenderModelToIR = new ReverseRenderIntermediateModel<>(
                modelParser,
                reverseRenderFunction)
                .withOverriddenId(MultiLevelGenerationModelStates.ReverseRender);

        final var stateGenerateSubproblemsForReverseRender = reverseRenderSubproblemDecompositionConfig.getSubproblemGeneratorImplementation().get()
                .withInputKey(MultiLevelModelStandardPayloadData.SerializedReverseRender)
                .withOutputKey(MultiLevelModelStandardPayloadData.SerializedReverseRender)
                .withSubproblemDecompositionEnabled(modelOptions.shouldPerformSubproblemDecomposition())
                .withOverriddenId(MultiLevelGenerationModelStates.GenerateReverseRenderSubproblems);

        final var stateExecuteDetailLevel = new PrepareAndSubmitMLRequestForDetailLevel<>(
                new PrepareAndSubmitMLRequestForDetailLevelParams<>(detailLevelPhaseConfig, contextProvider, modelPromptGenerator, MultiLevelModelPromptType.GenerateDetailLevel, componentLibrary))
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ExecuteDetailLevel);

        final var stateValidateDetailLevel = detailLevelPhaseConfig.createValidationStage(
                    detailLevelPhaseConfig.getModelSchema(), detailLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenId(MultiLevelGenerationModelStates.ValidateDetailLevel);

        final var stateCombineSubproblems = subproblemDecompositionConfig.getSubproblemCombinationImplementation().get()
                .withInputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOutputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withSubproblemDecompositionEnabled(modelOptions.shouldPerformSubproblemDecomposition())
                .withOverriddenId(MultiLevelGenerationModelStates.CombineSubproblems);

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

        final var states = List.of(stateInit, stateSanitizingPrePass, statePreprocessing, stateGenerateSubproblems,
                                   stateExecuteHighLevel, stateValidateHighLevel, stateReverseRenderModelToIR, stateGenerateSubproblemsForReverseRender,
                                   stateExecuteDetailLevel, stateValidateDetailLevel, stateCombineSubproblems, stateGenerateModel, stateComplete);

        // Complete initialization, and apply any global model state that the states want to consume
        states.forEach(ModelInterfaceState::completeStateInitialization);
        states.forEach(state -> state.applyModelOptions(options));

        // Transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(

                new ModelInterfaceTransitionRule(stateInit, UsePreprocessingFlow, stateSanitizingPrePass),
                new ModelInterfaceTransitionRule(stateInit, UseReverseRenderingFlow, stateReverseRenderModelToIR),

                new ModelInterfaceTransitionRule(stateSanitizingPrePass, StandardSignals.SUCCESS, statePreprocessing),
                new ModelInterfaceTransitionRule(stateSanitizingPrePass, StandardSignals.SKIPPED, statePreprocessing),  // Optional stage

                new ModelInterfaceTransitionRule(statePreprocessing, StandardSignals.SUCCESS, stateGenerateSubproblems),
                new ModelInterfaceTransitionRule(statePreprocessing, StandardSignals.SKIPPED, stateGenerateSubproblems),  // Optional stage

                new ModelInterfaceTransitionRule(stateGenerateSubproblems, StandardSignals.SUCCESS, stateExecuteHighLevel),

                new ModelInterfaceTransitionRule(stateExecuteHighLevel, StandardSignals.SUCCESS, stateValidateHighLevel),
                new ModelInterfaceTransitionRule(stateValidateHighLevel, StandardSignals.SUCCESS, stateExecuteDetailLevel),

                new ModelInterfaceTransitionRule(stateReverseRenderModelToIR, StandardSignals.SUCCESS, stateGenerateSubproblemsForReverseRender),
                new ModelInterfaceTransitionRule(stateReverseRenderModelToIR, StandardSignals.SKIPPED, stateGenerateSubproblemsForReverseRender), // Optional stage

                new ModelInterfaceTransitionRule(stateGenerateSubproblemsForReverseRender, StandardSignals.SUCCESS, stateExecuteDetailLevel),

                new ModelInterfaceTransitionRule(stateExecuteDetailLevel, StandardSignals.SUCCESS, stateValidateDetailLevel),
                new ModelInterfaceTransitionRule(stateExecuteDetailLevel, MultiLevelModelStandardSignals.ReturnToHighLevel, stateExecuteHighLevel), // LLM-directed retry for one-shot generation
                new ModelInterfaceTransitionRule(stateExecuteDetailLevel, MultiLevelModelStandardSignals.RetryDetailLevel, stateExecuteDetailLevel), // LLM-directed retry for multi-shot generation
                new ModelInterfaceTransitionRule(stateValidateDetailLevel, StandardSignals.SUCCESS, stateCombineSubproblems),


                new ModelInterfaceTransitionRule(stateCombineSubproblems, SubproblemDecompositionSignals.ProcessNextSubproblem, stateGenerateSubproblems),      // Iterate back to process next subproblem
                new ModelInterfaceTransitionRule(stateCombineSubproblems, SubproblemDecompositionSignals.ProcessNextIntermediateModelSubproblem, stateGenerateSubproblemsForReverseRender),
                new ModelInterfaceTransitionRule(stateCombineSubproblems, SubproblemDecompositionSignals.SubproblemDecompositionCompleted, stateGenerateModel), // All subproblems complete, so continue

                new ModelInterfaceTransitionRule(stateGenerateModel, StandardSignals.SUCCESS, stateComplete)
        ));

        return new ModelData(states, rules);
    }

    protected Class<? extends ModelInterfaceState> getInitialState() {
        return StartMultiLevelGeneration.class;
    }

    protected MultiLevelModelStandardSignals getStartSignal(String canvasModel) {
        if(canvasModel == null) {
            return UsePreprocessingFlow;
        } else {
            return UseReverseRenderingFlow;
        }
    }


}
