package org.rj.modelgen.bpmn.models.generation.multilevel;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.component.*;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.component.synthetic.types.BpmnSyntheticUnknownElementNode;
import org.rj.modelgen.bpmn.generation.BpmnModelGenerationFunction;
import org.rj.modelgen.bpmn.generation.BpmnReverseRenderFunction;
import org.rj.modelgen.bpmn.intrep.BpmnModelParser;
import org.rj.modelgen.bpmn.intrep.model.BpmnHighLevelIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.assets.BpmnIntermediateModelAssets;
import org.rj.modelgen.bpmn.intrep.validation.BpmnDetailLevelIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.intrep.validation.BpmnHighLevelIntermediateModelSanitizer;
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
import org.rj.modelgen.bpmn.models.generation.validation.ValidateBpmnModel;
import org.rj.modelgen.bpmn.subproblem.BpmnCombineSubproblems;
import org.rj.modelgen.bpmn.subproblem.BpmnGenerateSubproblems;
import org.rj.modelgen.llm.component.DefaultComponentLibrarySelector;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.context.provider.impl.DefaultContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModel;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModelStates;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelDetailPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultilevelModelPreprocessingConfig;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;

import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.models.generation.multilevel.states.ReverseRenderFunction;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachineCustomization;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardErrorSignals;
import org.rj.modelgen.llm.statemodel.states.common.PrepareAndSubmitLlmGenericRequest;
import org.rj.modelgen.llm.subproblem.config.SubproblemDecompositionConfig;
import org.rj.modelgen.bpmn.models.generation.base.signals.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BpmnMultiLevelGenerationModel extends MultiLevelGenerationModel<BpmnHighLevelIntermediateModel, BpmnIntermediateModel, BpmnIntermediateModelAssets,
        BpmnModelInstance, BpmnComponentLibrary,
        BpmnGenerationResult> {

    private final BpmnComponentLibrary componentLibrary;

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface, BpmnMultiLevelGenerationModelOptions options) {
        return create(modelInterface, options, new BpmnModelGenerationFunction(), new ValidateBpmnModel(BpmnComponentLibrary.defaultLibrary(), BpmnGlobalVariableLibrary.defaultLibrary()),
                BpmnComponentLibrary.defaultLibrary(), BpmnGlobalVariableLibrary.defaultLibrary());
    }

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface, BpmnMultiLevelGenerationModelOptions options, BpmnModelGenerationFunction modelGenerationFunction,
                                                       ValidateBpmnModel bpmnModelValidator, BpmnComponentLibrary componentLibrary, BpmnGlobalVariableLibrary globalVariableLibrary) {
        final var promptGenerator = new BpmnGenerationMultiLevelPromptGenerator();
        final var contextProvider = new DefaultContextProvider();

        final var preprocessingConfig = new MultilevelModelPreprocessingConfig<>(
                new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryPreprocessingLevelSerializer());

        final var highLevelConfig = new MultiLevelModelPhaseConfig<>(
                BpmnHighLevelIntermediateModel.class, BpmnIntermediateModelAssets.class, new BpmnGenerationMultiLevelSchemaHighLevel(),
                new BpmnHighLevelIntermediateModelSanitizer(), new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryHighLevelSerializer(),
                params -> new PrepareBpmnMLHighLevelModelGenerationRequest<>(params, globalVariableLibrary),
                null);

        final var reverseRenderFunction = new BpmnReverseRenderFunction();

        final var detailLevelConfig = new MultiLevelModelDetailPhaseConfig<>( // TODO
                BpmnIntermediateModel.class, BpmnIntermediateModelAssets.class, new BpmnGenerationMultiLevelSchemaDetailLevel(),
                new BpmnDetailLevelIntermediateModelSanitizer(), new BpmnComponentLibraryDetailLevelSelector(),
                new BpmnComponentLibraryDetailLevelSerializer(),
                params -> new PrepareBpmnMLDetailLevelModelGenerationRequest<>(params, globalVariableLibrary),
                null);

        final Function<BpmnModelInstance, String> renderedModelSerializer = Bpmn::convertToString;

        final var subproblemDecompositionConfig = SubproblemDecompositionConfig.defaultConfig()
                .withSubproblemGeneratorImplementation(BpmnGenerateSubproblems::new)
                .withSubproblemCombinationImplementation(BpmnCombineSubproblems::new);

        final var reverseRenderSubproblemDecompositionConfig = SubproblemDecompositionConfig.defaultConfig()
                .withSubproblemGeneratorImplementation(() -> new BpmnGenerateSubproblems()
                        .withInputKey(MultiLevelModelStandardPayloadData.SerializedReverseRender)
                        .withOutputKey(MultiLevelModelStandardPayloadData.SerializedReverseRender))
                .withSubproblemCombinationImplementation(BpmnCombineSubproblems::new);


        final var completionState = new BpmnGenerationComplete();

        final var bpmnModelParser = new BpmnModelParser();

        return (BpmnMultiLevelGenerationModel) new BpmnMultiLevelGenerationModel(BpmnMultiLevelGenerationModel.class, modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelConfig, reverseRenderFunction, detailLevelConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig, reverseRenderSubproblemDecompositionConfig, completionState, options, bpmnModelParser)
                .withModelCustomization(data -> addBpmnModelCustomization(data, options, promptGenerator, contextProvider, componentLibrary, globalVariableLibrary, bpmnModelValidator));
    }

    protected BpmnMultiLevelGenerationModel(Class<? extends BpmnMultiLevelGenerationModel> modelClass,
                                            ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                            ContextProvider contextProvider, BpmnComponentLibrary componentLibrary,
                                            MultilevelModelPreprocessingConfig<BpmnComponentLibrary> preprocessingConfig,
                                            MultiLevelModelPhaseConfig<BpmnHighLevelIntermediateModel, BpmnIntermediateModelAssets, BpmnComponentLibrary, ?, ?, ?> highLevelPhaseConfig,
                                            ReverseRenderFunction<BpmnModelInstance, BpmnIntermediateModel> reverseRenderFunction,
                                            MultiLevelModelDetailPhaseConfig<BpmnIntermediateModel, BpmnIntermediateModelAssets, BpmnComponentLibrary, ?, ?, ?> detailLevelPhaseConfig,
                                            ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> modelGenerationFunction,
                                            Function<BpmnModelInstance, String> renderedModelSerializer,
                                            SubproblemDecompositionConfig subproblemDecompositionConfig,
                                            SubproblemDecompositionConfig reverseRenderSubproblemDecompositionConfig,
                                            ModelInterfaceState completionState,
                                            BpmnMultiLevelGenerationModelOptions options,
                                            BpmnModelParser bpmnModelParser) {

        super(BpmnMultiLevelGenerationModel.class, modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelPhaseConfig, reverseRenderFunction, detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig,
                reverseRenderSubproblemDecompositionConfig, completionState, options, bpmnModelParser);
        this.componentLibrary = componentLibrary;
    }

    @Override
    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request, String canvasModel, Map<String, Object> data) {
        final var initialState = MultiLevelGenerationModelStates.StartMultiLevelGeneration.toString();

        BpmnGenerationModelInputPayload input = new BpmnGenerationModelInputPayload(sessionId, request, canvasModel);
        if (data != null) input.putAllIfAbsent(data);

        return this.execute(initialState, getStartSignal(canvasModel), input)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }

    private static ModelInterfaceStateMachineCustomization addBpmnModelCustomization(ModelCustomizationData modelData, BpmnMultiLevelGenerationModelOptions options, BpmnGenerationMultiLevelPromptGenerator promptGenerator, ContextProvider contextProvider,
                                                                                     BpmnComponentLibrary componentLibrary, BpmnGlobalVariableLibrary globalVariableLibrary, ValidateBpmnModel bpmnModelValidator) {
        final List<BiFunction<ModelInterfaceStateMachineCustomization, ModelCustomizationData, ModelInterfaceStateMachineCustomization>> customizations = List.of(
                (customization, data) -> initializeBpmnPayload(customization, contextProvider, promptGenerator, componentLibrary),
                (customization, data) -> initializeBpmnData(customization, data, componentLibrary, globalVariableLibrary, options),
                (customization, data) -> preProcessingInsertSyntheticComponents(customization, data, options),
                (customization, data) -> processHighLevelModelDataForDetailLevelGeneration(customization, data, globalVariableLibrary),
                (customization, data) -> validateDetailLevelModel(customization, data, globalVariableLibrary, bpmnModelValidator),
                BpmnMultiLevelGenerationModel::postProcessingResolveSyntheticComponents,
                (customization, data) -> postProcessingPrepareForRendering(customization, data, globalVariableLibrary),
                BpmnMultiLevelGenerationModel::validateBpmnModelCorrectness
        );

        // Apply each customization in turn and return the full result
        return customizations.stream()
                .reduce(new ModelInterfaceStateMachineCustomization(),
                        (customization, f) -> f.apply(customization, modelData),
                        (a, b) -> a);
    }

    private static ModelInterfaceStateMachineCustomization initializeBpmnPayload(ModelInterfaceStateMachineCustomization customization, ContextProvider contextProvider,
                                                                                 BpmnGenerationMultiLevelPromptGenerator promptGenerator, BpmnComponentLibrary componentLibrary) {
        final var initializeProcessPayload = new PrepareAndSubmitLlmGenericRequest<>(contextProvider, promptGenerator, MultiLevelModelPromptType.PayloadGeneration, componentLibrary)
                .withResponseOutputKey(MultiLevelModelStandardPayloadData.ProcessVariables)
                .withOverriddenId(BpmnAdditionalModelStates.InitializeBpmnPayload);

        return customization
                .withNewStateInsertedAfter(initializeProcessPayload, MultiLevelGenerationModelStates.SanitizingPrePass.toString());
    }

    private static ModelInterfaceStateMachineCustomization initializeBpmnData(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData,
                                                                              BpmnComponentLibrary componentLibrary, BpmnGlobalVariableLibrary globalVariableLibrary, BpmnMultiLevelGenerationModelOptions options) {
        final var initializeData = new InitializeBpmnData(componentLibrary, globalVariableLibrary, options)
                .withOverriddenId(BpmnAdditionalModelStates.InitializeBpmnData);

        return customization
                .withNewStateInsertedAfter(initializeData, BpmnAdditionalModelStates.InitializeBpmnPayload.toString());
    }

    private static ModelInterfaceStateMachineCustomization preProcessingInsertSyntheticComponents(ModelInterfaceStateMachineCustomization customization,
                                                                                                  ModelCustomizationData modelData, BpmnMultiLevelGenerationModelOptions options) {
        final var syntheticComponents = BpmnComponentLibrary.defaultSyntheticComponentsLibrary();

        // Only include "unknown component" synthetic component if we have enabled insert of placeholders for unsupported components
        if (!options.shouldAddPlaceholderForUnknownComponents()) {
            syntheticComponents.getComponents().removeIf(
                    a -> BpmnSyntheticUnknownElementNode.NODE_TYPE.equals(a.getName()));
        }

        final var insertSyntheticComponents = new InsertSyntheticBpmnComponents(syntheticComponents.getComponents())
                .withOverriddenId(BpmnAdditionalModelStates.InsertSyntheticComponents);

        return customization
                .withNewStateInsertedAfter(insertSyntheticComponents, BpmnAdditionalModelStates.InitializeBpmnData.toString());
    }

    private static ModelInterfaceStateMachineCustomization processHighLevelModelDataForDetailLevelGeneration(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData, BpmnGlobalVariableLibrary globalVariableLibrary) {
        final var processHighLevelData = new ProcessHighLevelModelDataForDetailLevelGeneration(globalVariableLibrary)
                .withOverriddenId(BpmnAdditionalModelStates.ProcessHighLevelModelDataForDetailLevelGeneration);

        return customization
                .withNewStateInsertedAfter(processHighLevelData, MultiLevelGenerationModelStates.ValidateHighLevel.toString());
    }

    private static ModelInterfaceStateMachineCustomization validateDetailLevelModel(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData, BpmnGlobalVariableLibrary globalVariableLibrary, ValidateBpmnModel bpmnModelValidator) {

        final var validateBpmnDetailLevelIntermediateModel = new ValidateBpmnLlmDetailLevelIntermediateModelResponse(globalVariableLibrary, bpmnModelValidator)
                .withOverriddenId(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation);

        validateBpmnDetailLevelIntermediateModel.setInvokeLimit(3);

        return customization
                .withNewStateInsertedAfter(validateBpmnDetailLevelIntermediateModel, MultiLevelGenerationModelStates.ValidateDetailLevel.toString())
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString(), BpmnGenerationSignals.IntermediateModelIsInvalid.toString(), MultiLevelGenerationModelStates.ExecuteDetailLevel.toString()))
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString(), StandardErrorSignals.FAILED_MAX_INVOCATIONS, BpmnAdditionalModelStates.ResolveSyntheticComponents.toString()));
    }

    private static ModelInterfaceStateMachineCustomization postProcessingResolveSyntheticComponents(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {
        final var resolveSyntheticActions = new ResolveSyntheticBpmnComponents()
                .withOverriddenId(BpmnAdditionalModelStates.ResolveSyntheticComponents);

        return customization
                .withNewStateInsertedAfter(resolveSyntheticActions, BpmnAdditionalModelStates.DetailLevelBpmnIRModelValidation.toString());
    }

    private static ModelInterfaceStateMachineCustomization postProcessingPrepareForRendering(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData, BpmnGlobalVariableLibrary globalVariableLibrary) {
        final var prepareForRendering = new PrepareBpmnModelForRendering(globalVariableLibrary)
                .withOverriddenId(BpmnAdditionalModelStates.PrepareForRendering);

        return customization
                .withNewStateInsertedAfter(prepareForRendering, BpmnAdditionalModelStates.ResolveSyntheticComponents.toString());
    }

    private static ModelInterfaceStateMachineCustomization validateBpmnModelCorrectness(ModelInterfaceStateMachineCustomization customization, ModelCustomizationData modelData) {
        final var validateBpmnModelCorrectness = new ValidateBpmnModelCorrectness()
                .withOverriddenId(BpmnAdditionalModelStates.ValidateBpmnModelCorrectness);

        return customization
                .withNewStateInsertedAfter(validateBpmnModelCorrectness, MultiLevelGenerationModelStates.GenerateModel.toString())
                .withNewRule(new ModelInterfaceTransitionRule.Reference(BpmnAdditionalModelStates.ValidateBpmnModelCorrectness.toString(), BpmnGenerationSignals.CompleteGeneration.toString(), MultiLevelGenerationModelStates.Complete.toString()));
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
