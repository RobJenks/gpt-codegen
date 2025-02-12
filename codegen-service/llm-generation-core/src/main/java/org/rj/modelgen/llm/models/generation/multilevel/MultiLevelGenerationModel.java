package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevel;
import org.rj.modelgen.llm.models.generation.multilevel.states.StartMultiLevelGeneration;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenericRequest;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.GenerateModelFromIntermediateModelTransformer;
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
                                                TResult> extends ModelInterfaceStateMachine {

    public MultiLevelGenerationModel(ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                     ContextProvider contextProvider, TComponentLibrary componentLibrary,
                                     MultiLevelModelPhaseConfig<THighLevelModel, TComponentLibrary> highLevelPhaseConfig,
                                     MultiLevelModelPhaseConfig<TDetailLevelModel, TComponentLibrary> detailLevelPhaseConfig,
                                     ModelGenerationFunction<TDetailLevelModel, TModel> modelGenerationFunction,
                                     Function<TModel, String> renderedModelSerializer,
                                     ModelInterfaceState completionState,
                                     MultiLevelGenerationModelOptions options) {
        this(modelInterface, buildModelData(promptGenerator, contextProvider, componentLibrary, highLevelPhaseConfig,
                detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, completionState, options));
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
            Function<TModel, String> renderedModelSerializer,
            ModelInterfaceState completionState,
            MultiLevelGenerationModelOptions options) {

        final var stateInit = new StartMultiLevelGeneration();

        // Overrides from model options
        final var modelOptions = Optional.ofNullable(options).orElseGet(MultiLevelGenerationModelOptions::defaultOptions);
        final MultiLevelGenerationModelPromptGenerator modelPromptGenerator = Optional.ofNullable(modelOptions.getPromptGeneratorOverride()).orElse(promptGenerator);
        final ModelSchema highLevelSchema = Optional.ofNullable(modelOptions.getHighLevelSchemaOverride()).orElse(highLevelPhaseConfig.getModelSchema());
        final ModelSchema detailLevelSchema = Optional.ofNullable(modelOptions.getDetailLevelSchemaOverride()).orElse(detailLevelPhaseConfig.getModelSchema());

        // Build each model state
        final var statePrePass = new PrepareAndSubmitLlmGenericRequest<>(contextProvider, promptGenerator, MultiLevelModelPromptType.PrePass1)
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.SanitizedPrompt)
                .withOverriddenModelSuccessResponse(Util.loadStringResource("generation-examples/multiLevel/example1/2-response-prepass.txt"))
                .withOverriddenId("sanitizingPrePass");

        final var stateExecuteHighLevel = new PrepareAndSubmitMLRequestForLevel<>(highLevelSchema, contextProvider,
                highLevelPhaseConfig.getModelSanitizer(), modelPromptGenerator, MultiLevelModelPromptType.GenerateHighLevel,
                componentLibrary, highLevelPhaseConfig.getComponentLibrarySelector(), highLevelPhaseConfig.getComponentLibrarySerializer())
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenModelSuccessResponse(Util.loadStringResource("generation-examples/multiLevel/example1/4-response-high-level.json"))
                .withOverriddenId("executeHighLevel");

        final var stateValidateHighLevel = new ValidateLlmIntermediateModelResponse(
                highLevelSchema, highLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.HighLevelModel)
                .withOverriddenId("validateHighLevel");

        final var stateExecuteDetailLevel = new PrepareAndSubmitMLRequestForLevel<>(detailLevelSchema, contextProvider,
                detailLevelPhaseConfig.getModelSanitizer(), modelPromptGenerator, MultiLevelModelPromptType.GenerateDetailLevel,
                componentLibrary, detailLevelPhaseConfig.getComponentLibrarySelector(), detailLevelPhaseConfig.getComponentLibrarySerializer())
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenModelSuccessResponse(Util.loadStringResource("generation-examples/multiLevel/example1/6-response-detail-level.json"))
                .withOverriddenId("executeDetailLevel");
        //final var stateReturnToHighLevelIfRequired = new { ... } // TODO
        final var stateValidateDetailLevel = new ValidateLlmIntermediateModelResponse(
                detailLevelSchema, detailLevelPhaseConfig.getIntermediateModelClass())
                .withModelInputKey(MultiLevelModelStandardPayloadData.DetailLevelModel)
                .withOverriddenId("validateDetailLevel");

        final var stateGenerateModel = new GenerateModelFromIntermediateModelTransformer<>(
                GenerateModelFromIntermediateModelTransformer.class,
                detailLevelPhaseConfig.getIntermediateModelClass(),
                MultiLevelModelStandardPayloadData.DetailLevelModel.toString(), // Input
                StandardModelData.GeneratedModel.toString(),                    // Output
                modelGenerationFunction, renderedModelSerializer);

        // final var stateValidateModelCorrectness = new { ... } // TODO
        final var stateComplete = completionState;

        final var states = List.of(stateInit, statePrePass, stateExecuteHighLevel, stateValidateHighLevel, stateExecuteDetailLevel,
                stateValidateDetailLevel, stateGenerateModel, stateComplete);

        // Transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule(stateInit, StandardSignals.SUCCESS, statePrePass),
                new ModelInterfaceTransitionRule(statePrePass, StandardSignals.SUCCESS, stateExecuteHighLevel),
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
