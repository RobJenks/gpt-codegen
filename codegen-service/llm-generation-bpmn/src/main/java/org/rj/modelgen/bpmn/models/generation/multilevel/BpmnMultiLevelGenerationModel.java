package org.rj.modelgen.bpmn.models.generation.multilevel;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.component.*;
import org.rj.modelgen.bpmn.generation.BpmnModelGenerationFunction;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModel;
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
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

public class BpmnMultiLevelGenerationModel extends MultiLevelGenerationModel<BpmnIntermediateModel, BpmnIntermediateModel,
                                                                             BpmnModelInstance, BpmnComponentLibrary,
                                                                             BpmnGenerationResult>
                                           implements BpmnGenerationExecutionModel {

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface, MultiLevelGenerationModelOptions options) {
        final var promptGenerator = new BpmnGenerationMultiLevelPromptGenerator();
        final var contextProvider = new DefaultContextProvider();
        final var componentLibrary = BpmnComponentLibrary.defaultLibrary();

        final var highLevelConfig = new MultiLevelModelPhaseConfig<>(
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaHighLevel(),
                new BpmnIntermediateModelSanitizer(), new DefaultComponentLibrarySelector<>(),
                new BpmnComponentLibraryHighLevelSerializer());

        final var detailLevelConfig = new MultiLevelModelPhaseConfig<>( // TODO
                BpmnIntermediateModel.class, new BpmnGenerationMultiLevelSchemaDetailLevel(),
                new BpmnIntermediateModelSanitizer(), new BpmnComponentLibraryDetailLevelSelector(),
                new BpmnComponentLibraryDetailLevelSerializer());

        final var modelGenerationFunction = new BpmnModelGenerationFunction();
        final Function<BpmnModelInstance, String> renderedModelSerializer = Bpmn::convertToString;

        final var completionState = new BpmnGenerationComplete();

        return new BpmnMultiLevelGenerationModel(modelInterface, promptGenerator, contextProvider, componentLibrary,
                highLevelConfig, detailLevelConfig, modelGenerationFunction, renderedModelSerializer, completionState, options);
    }

    private BpmnMultiLevelGenerationModel(ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                         ContextProvider contextProvider, BpmnComponentLibrary componentLibrary,
                                         MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponentLibrary> highLevelPhaseConfig,
                                         MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponentLibrary> detailLevelPhaseConfig,
                                         ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> modelGenerationFunction,
                                         Function<BpmnModelInstance, String> renderedModelSerializer,
                                         ModelInterfaceState completionState,
                                         MultiLevelGenerationModelOptions options) {

        super(modelInterface, promptGenerator, contextProvider, componentLibrary, highLevelPhaseConfig,
              detailLevelPhaseConfig, modelGenerationFunction, renderedModelSerializer, completionState, options);
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
        // Optional model response overrides which can be enabled for testing without LLM integration
        return (MultiLevelGenerationModelOptions)MultiLevelGenerationModelOptions.defaultOptions()
                //  .withOverriddenLlmResponse(MultiLevelGenerationModelStates.SanitizingPrePass, Util.loadStringResource("generation-examples/multiLevel/example1/1b-sanitizing-prepass-response.txt"), ModelResponse.Status.SUCCESS)
                //  .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteHighLevel, Util.loadStringResource("generation-examples/multiLevel/example1/3b-high-level-response.json"), ModelResponse.Status.SUCCESS)
                //  .withOverriddenLlmResponse(MultiLevelGenerationModelStates.ExecuteDetailLevel, Util.loadStringResource("generation-examples/multiLevel/example1/4b-detail-level-response.json"), ModelResponse.Status.SUCCESS)
                ;
    }
}
