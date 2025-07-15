package org.rj.modelgen.bpmn.models.generation.multilevel;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.BpmnComponentLibraryDetailLevelSelector;
import org.rj.modelgen.bpmn.component.BpmnComponentLibraryDetailLevelSerializer;
import org.rj.modelgen.bpmn.component.BpmnComponentLibraryHighLevelSerializer;
import org.rj.modelgen.bpmn.generation.BpmnModelGenerationFunction;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.bpmn.models.generation.base.data.BpmnGenerationModelInputPayload;
import org.rj.modelgen.bpmn.models.generation.base.states.BpmnGenerationComplete;
import org.rj.modelgen.bpmn.models.generation.multilevel.prompt.BpmnGenerationMultiLevelPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.multilevel.schema.BpmnGenerationMultiLevelSchemaDetailLevel;
import org.rj.modelgen.bpmn.models.generation.multilevel.schema.BpmnGenerationMultiLevelSchemaHighLevel;
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
import org.rj.modelgen.llm.models.generation.options.OverriddenLlmResponse;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.subproblem.config.SubproblemDecompositionConfig;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BpmnMultiLevelGenerationModel extends MultiLevelGenerationModel<BpmnIntermediateModel, BpmnIntermediateModel,
                                                                             BpmnModelInstance, BpmnComponentLibrary,
                                                                             BpmnGenerationResult> {

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface, MultiLevelGenerationModelOptions options) {
        final var promptGenerator = new BpmnGenerationMultiLevelPromptGenerator();
        final var contextProvider = new DefaultContextProvider();
        final var componentLibrary = BpmnComponentLibrary.defaultLibrary();

        final var preprocessingConfig = MultilevelModelPreprocessingConfig.<BpmnComponentLibrary>defaultConfig();

        final var highLevelConfig = new MultiLevelModelPhaseConfig.Basic<>(
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaHighLevel(),
                new BpmnIntermediateModelSanitizer(), new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryHighLevelSerializer());

        final var detailLevelConfig = new MultiLevelModelPhaseConfig.Basic<>( // TODO
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaDetailLevel(),
                new BpmnIntermediateModelSanitizer(), new BpmnComponentLibraryDetailLevelSelector(),
                new BpmnComponentLibraryDetailLevelSerializer());

        final var modelGenerationFunction = new BpmnModelGenerationFunction();
        final Function<BpmnModelInstance, String> renderedModelSerializer = Bpmn::convertToString;

        final var subproblemDecompositionConfig = SubproblemDecompositionConfig.defaultConfig();

        final var completionState = new BpmnGenerationComplete();

        return new BpmnMultiLevelGenerationModel(modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelConfig, detailLevelConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig, completionState, options);
    }

    private BpmnMultiLevelGenerationModel(ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                         ContextProvider contextProvider, BpmnComponentLibrary componentLibrary,
                                          MultilevelModelPreprocessingConfig<BpmnComponentLibrary> preprocessingConfig,
                                         MultiLevelModelPhaseConfig.Basic<BpmnIntermediateModel, BpmnComponentLibrary> highLevelPhaseConfig,
                                         MultiLevelModelPhaseConfig.Basic<BpmnIntermediateModel, BpmnComponentLibrary> detailLevelPhaseConfig,
                                         ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> modelGenerationFunction,
                                         Function<BpmnModelInstance, String> renderedModelSerializer,
                                         SubproblemDecompositionConfig subproblemDecompositionConfig,
                                         ModelInterfaceState completionState,
                                         MultiLevelGenerationModelOptions options) {

        super(BpmnMultiLevelGenerationModel.class, modelInterface, promptGenerator, contextProvider, componentLibrary, preprocessingConfig,
                highLevelPhaseConfig, detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, subproblemDecompositionConfig,
                completionState, options);
    }

    @Override
    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request, Map<String, Object> data) {
        final var initialState = MultiLevelGenerationModelStates.StartMultiLevelGeneration.toString();

        BpmnGenerationModelInputPayload input = new BpmnGenerationModelInputPayload(sessionId, request);
        if (data != null) input.putAllIfAbsent(data);

        return this.execute(initialState, getStartSignal(), input)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }

    public static MultiLevelGenerationModelOptions defaultOptions() {
        return (MultiLevelGenerationModelOptions)MultiLevelGenerationModelOptions.defaultOptions()
                .withPerformSubproblemDecomposition(false)

                // Optional model response overrides which can be enabled for testing without LLM integration
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.SanitizingPrePass, Util.loadStringResource("generation-examples/multiLevel/example1/1b-sanitizing-prepass-response.txt"), ModelResponse.Status.SUCCESS)
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteHighLevel, Util.loadStringResource("generation-examples/multiLevel/example1/3b-high-level-response.json"), ModelResponse.Status.SUCCESS)
                // .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteDetailLevel, Util.loadStringResource("generation-examples/multiLevel/example1/4b-detail-level-response.json"), ModelResponse.Status.SUCCESS)
                ;
    }
}
