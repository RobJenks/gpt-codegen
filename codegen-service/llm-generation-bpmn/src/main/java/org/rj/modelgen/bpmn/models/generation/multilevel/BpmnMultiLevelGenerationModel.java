package org.rj.modelgen.bpmn.models.generation.multilevel;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.component.*;
import org.rj.modelgen.bpmn.component.synthetic.types.BpmnSyntheticUnknownElementNode;
import org.rj.modelgen.bpmn.generation.BpmnModelGenerationFunction;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.bpmn.models.generation.base.data.BpmnGenerationModelInputPayload;
import org.rj.modelgen.bpmn.models.generation.base.states.BpmnGenerationComplete;
import org.rj.modelgen.bpmn.models.generation.common.BpmnAdditionalModelStates;
import org.rj.modelgen.bpmn.models.generation.common.states.ValidateBpmnModelCorrectness;
import org.rj.modelgen.bpmn.models.generation.multilevel.options.BpmnMultiLevelGenerationModelOptions;
import org.rj.modelgen.bpmn.models.generation.multilevel.prompt.BpmnGenerationMultiLevelPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.multilevel.schema.BpmnGenerationMultiLevelSchemaDetailLevel;
import org.rj.modelgen.bpmn.models.generation.multilevel.schema.BpmnGenerationMultiLevelSchemaHighLevel;
import org.rj.modelgen.bpmn.models.generation.multilevel.states.*;
import org.rj.modelgen.bpmn.subproblem.BpmnCombineSubproblems;
import org.rj.modelgen.bpmn.subproblem.BpmnGenerateSubproblems;
import org.rj.modelgen.llm.component.DefaultComponentLibrarySelector;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.context.provider.impl.DefaultContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModel;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModelOptions;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModelStates;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultilevelModelPreprocessingConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;

import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachineCustomization;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.statemodel.signals.common.StandardErrorSignals;
import org.rj.modelgen.llm.subproblem.config.SubproblemDecompositionConfig;
import org.rj.modelgen.bpmn.models.generation.base.signals.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BpmnMultiLevelGenerationModel extends MultiLevelGenerationModel<BpmnIntermediateModel, BpmnIntermediateModel,
                                                                             BpmnModelInstance, BpmnComponentLibrary,
                                                                             BpmnGenerationResult> {

    private final BpmnComponentLibrary componentLibrary;// = BpmnComponentLibrary.defaultLibrary();

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface, BpmnMultiLevelGenerationModelOptions options) {
        final var promptGenerator = new BpmnGenerationMultiLevelPromptGenerator();
        final var contextProvider = new DefaultContextProvider();
        final var componentLibrary = BpmnComponentLibrary.defaultLibrary();

        final var preprocessingConfig = new MultilevelModelPreprocessingConfig<>(
                new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryPreprocessingLevelSerializer());

        final var highLevelConfig = new MultiLevelModelPhaseConfig<>(
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaHighLevel(),
                new BpmnIntermediateModelSanitizer(), new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryHighLevelSerializer(),
                PrepareBpmnMLHighLevelModelGenerationRequest::new,
                null);

        final var detailLevelConfig = new MultiLevelModelPhaseConfig<>( // TODO
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaDetailLevel(),
                new BpmnIntermediateModelSanitizer(), new BpmnComponentLibraryDetailLevelSelector(),
                new BpmnComponentLibraryDetailLevelSerializer(),
                PrepareBpmnMLDetailLevelModelGenerationRequest::new,
                null);

        final var modelGenerationFunction = new BpmnModelGenerationFunction();
        final Function<BpmnModelInstance, String> renderedModelSerializer = Bpmn::convertToString;

        final var subproblemDecompositionConfig = SubproblemDecompositionConfig.defaultConfig() // missing subproblems
            .withSubproblemGeneratorImplementation(BpmnGenerateSubproblems::new) // might create a bpmn version of the subclass
                .withSubproblemCombinationImplementation(BpmnCombineSubproblems::new); // might not be needed for bpmn

        final var completionState = new BpmnGenerationComplete();

        return (BpmnMultiLevelGenerationModel) new BpmnMultiLevelGenerationModel(BpmnMultiLevelGenerationModel.class, modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelConfig, detailLevelConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig, completionState, options)
                .withModelCustomization(data -> addBpmnModelCustomization(data, options));
    }

    protected BpmnMultiLevelGenerationModel(Class<? extends BpmnMultiLevelGenerationModel> modelClass,
                                            ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                            ContextProvider contextProvider, BpmnComponentLibrary componentLibrary,
                                            MultilevelModelPreprocessingConfig<BpmnComponentLibrary> preprocessingConfig,
                                            MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponentLibrary, ?, ?> highLevelPhaseConfig,
                                            MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponentLibrary, ?, ?> detailLevelPhaseConfig,
                                            ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> modelGenerationFunction,
                                            Function<BpmnModelInstance, String> renderedModelSerializer,
                                            SubproblemDecompositionConfig subproblemDecompositionConfig,
                                            ModelInterfaceState completionState,
                                            BpmnMultiLevelGenerationModelOptions options) {

        super(BpmnMultiLevelGenerationModel.class, modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelPhaseConfig, detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig,
                completionState, options);
        this.componentLibrary = componentLibrary;
    }

    @Override
    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request, Map<String, Object> data) {
        final var initialState = MultiLevelGenerationModelStates.StartMultiLevelGeneration.toString();

        BpmnGenerationModelInputPayload input = new BpmnGenerationModelInputPayload(sessionId, request);
        if (data != null) input.putAllIfAbsent(data);

        return this.execute(initialState, getStartSignal(), input)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }

    // missing model customisation
    private static ModelInterfaceStateMachineCustomization addBpmnModelCustomization(ModelCustomizationData modelData, BpmnMultiLevelGenerationModelOptions options) {
        final List<BiFunction<ModelInterfaceStateMachineCustomization, ModelCustomizationData, ModelInterfaceStateMachineCustomization>> customizations = List.of(
                //(customization, data) -> initializeBpmnData(customization, data, options), // hrs only so not needed
                (customization, data) -> preProcessingInsertSyntheticComponents(customization, data, options), // might be used for both models... add it to base lib
                //BpmnMultiLevelGenerationModel::preProcessingValidation, // hrs only for now
                //BpmnMultiLevelGenerationModel::processHighLevelModelDataForDetailLevelGeneration, // hrs only as it uses global variables - might be added in the future for bpmn
                BpmnMultiLevelGenerationModel::validateDetailLevelModel, // implementation hrs specific but will be used for both so add to base (base library level)
                BpmnMultiLevelGenerationModel::postProcessingResolveSyntheticComponents, //make it for both and add to base multilevel
                BpmnMultiLevelGenerationModel::postProcessingPrepareForRendering//, // hrs specific right now but will be needed for bpmn so make it general step for both
                //BpmnMultiLevelGenerationModel::validateBpmnModelCorrectness
        );

        // Apply each customization in turn and return the full result
        return customizations.stream()
                .reduce(new ModelInterfaceStateMachineCustomization(),
                        (customization, f) -> f.apply(customization, modelData),
                        (a, b) -> a);
    }

    private static ModelInterfaceStateMachineCustomization preProcessingInsertSyntheticComponents(ModelInterfaceStateMachineCustomization customization,
                                                                                               ModelCustomizationData modelData, BpmnMultiLevelGenerationModelOptions options) {
        final var syntheticComponents = BpmnComponentLibrary.defaultLibrary();

        // Only include "unknown component" synthetic component if we have enabled insert of placeholders for unsupported components
        if (!options.shouldAddPlaceholderForUnknownComponents()) {
            syntheticComponents.getComponents().removeIf(
                    a -> BpmnSyntheticUnknownElementNode.NODE_TYPE.equals(a.getName()));
        }

        final var insertSyntheticComponents = new InsertSyntheticBpmnComponents(syntheticComponents.getComponents()) //TODO: ? class finished I think
                .withOverriddenId(BpmnAdditionalModelStates.InsertSyntheticComponents); //TODO: ? would bpmns have the same model states as hrs?

        return customization
                .withNewStateInsertedAfter(insertSyntheticComponents, MultiLevelGenerationModelStates.SanitizingPrePass.toString());
    }

    private static ModelInterfaceStateMachineCustomization validateDetailLevelModel(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {

        final var validateBpmnDetailLevelIntermediateModel = new ValidateBpmnLlmDetailLevelIntermediateModelResponse() //TODO: ? finish validation class - how to validate bpmn model / is it the same as validateBpmnModelCorrectness?
                .withOverriddenId(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation);

        validateBpmnDetailLevelIntermediateModel.setInvokeLimit(3);

        return customization
                .withNewStateInsertedAfter(validateBpmnDetailLevelIntermediateModel, MultiLevelGenerationModelStates.ValidateDetailLevel.toString())
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString(), BpmnGenerationSignals.ValidateBpmnXml.toString(), MultiLevelGenerationModelStates.ExecuteDetailLevel.toString()))
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString(), StandardErrorSignals.FAILED_MAX_INVOCATIONS, BpmnAdditionalModelStates.ResolveSyntheticComponents.toString()));
    }

    private static ModelInterfaceStateMachineCustomization postProcessingResolveSyntheticComponents(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {
        final var resolveSyntheticActions = new ResolveSyntheticBpmnComponents() //TODO: ? class finished - confirm unknown element resolving logic
                .withOverriddenId(BpmnAdditionalModelStates.ResolveSyntheticComponents);

        return customization
                .withNewStateInsertedAfter(resolveSyntheticActions, BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString());
    }

    private static ModelInterfaceStateMachineCustomization postProcessingPrepareForRendering(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {
        final var prepareForRendering = new PrepareBpmnModelForRendering() //TODO: ? finish the class - how much updates to make it work for bpmn
                .withOverriddenId(BpmnAdditionalModelStates.PrepareForRendering);

        return customization
                .withNewStateInsertedAfter(prepareForRendering, BpmnAdditionalModelStates.ResolveSyntheticComponents.toString());
    }

    private static ModelInterfaceStateMachineCustomization validateBpmnModelCorrectness(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {
        final var validateBpmnModelCorrectness = new ValidateBpmnModelCorrectness()
                .withOverriddenId(BpmnAdditionalModelStates.ValidateBpmnModelCorrectness);

        return customization
                .withNewStateInsertedAfter(validateBpmnModelCorrectness, BpmnAdditionalModelStates.PrepareForRendering.toString())
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.ValidateBpmnModelCorrectness.toString(), BpmnGenerationSignals.ValidateBpmnXml.toString(), MultiLevelGenerationModelStates.Complete.toString()));
    }

    public static BpmnMultiLevelGenerationModelOptions defaultOptions() {
        return (BpmnMultiLevelGenerationModelOptions) BpmnMultiLevelGenerationModelOptions.defaultOptions()
                .withPerformSubproblemDecomposition(false)

                // Optional model response overrides which can be enabled for testing without LLM integration
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.SanitizingPrePass, Util.loadStringResource("generation-examples/multiLevel/example1/1b-sanitizing-prepass-response.txt"), ModelResponse.Status.SUCCESS)
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteHighLevel, Util.loadStringResource("generation-examples/multiLevel/example1/3b-high-level-response.json"), ModelResponse.Status.SUCCESS)
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteDetailLevel, Util.loadStringResource("generation-examples/multiLevel/example1/4b-detail-level-response.json"), ModelResponse.Status.SUCCESS)
                ;
    }

    public BpmnComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }
}
