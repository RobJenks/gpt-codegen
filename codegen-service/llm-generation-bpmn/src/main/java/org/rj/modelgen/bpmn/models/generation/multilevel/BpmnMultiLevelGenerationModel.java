package org.rj.modelgen.bpmn.models.generation.multilevel;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.BpmnComponentLibraryDetailLevelSerializer;
import org.rj.modelgen.bpmn.component.BpmnComponentLibraryHighLevelSerializer;
import org.rj.modelgen.bpmn.generation.BpmnModelGenerationFunction;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.schema.BpmnIntermediateModelSchema;
import org.rj.modelgen.bpmn.intrep.validation.BpmnIntermediateModelSanitizer;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.bpmn.models.generation.base.data.BpmnGenerationModelInputPayload;
import org.rj.modelgen.bpmn.models.generation.base.states.BpmnGenerationComplete;
import org.rj.modelgen.bpmn.models.generation.multilevel.prompt.BpmnGenerationMultiLevelPromptGenerator;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.context.provider.impl.DefaultContextProvider;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModel;
import org.rj.modelgen.llm.models.generation.multilevel.config.MultiLevelModelPhaseConfig;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class BpmnMultiLevelGenerationModel extends MultiLevelGenerationModel<BpmnIntermediateModel, BpmnIntermediateModel,
                                                                             BpmnModelInstance, BpmnComponent, BpmnGenerationResult> {

    public static BpmnMultiLevelGenerationModel create(ModelInterface modelInterface) {
        final var promptGenerator = new BpmnGenerationMultiLevelPromptGenerator();
        final var contextProvider = new DefaultContextProvider();
        final var componentLibrary = BpmnComponentLibrary.defaultLibrary();

        final var highLevelConfig = new MultiLevelModelPhaseConfig<>(
                BpmnIntermediateModel.class, new BpmnIntermediateModelSchema(),
                new BpmnIntermediateModelSanitizer(), new BpmnComponentLibraryHighLevelSerializer());

        final var detailLevelConfig = new MultiLevelModelPhaseConfig<>( // TODO
                BpmnIntermediateModel.class, new BpmnIntermediateModelSchema(),
                new BpmnIntermediateModelSanitizer(), new BpmnComponentLibraryDetailLevelSerializer());

        final var modelGenerationFunction = new BpmnModelGenerationFunction();

        final var completionState = new BpmnGenerationComplete();

        return new BpmnMultiLevelGenerationModel(modelInterface, promptGenerator, contextProvider, componentLibrary,
                highLevelConfig, detailLevelConfig, modelGenerationFunction, completionState);
    }

    private BpmnMultiLevelGenerationModel(ModelInterface modelInterface, MultiLevelGenerationModelPromptGenerator promptGenerator,
                                         ContextProvider contextProvider, ComponentLibrary<BpmnComponent> componentLibrary,
                                         MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponent> highLevelPhaseConfig,
                                         MultiLevelModelPhaseConfig<BpmnIntermediateModel, BpmnComponent> detailLevelPhaseConfig,
                                         ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> modelGenerationFunction,
                                         ModelInterfaceState completionState) {

        super(modelInterface, promptGenerator, contextProvider, componentLibrary, highLevelPhaseConfig,
              detailLevelPhaseConfig, modelGenerationFunction, completionState);
    }

    @Override
    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request) {
        final var initialState = ModelInterfaceState.defaultStateId(getInitialState());
        final var input = new BpmnGenerationModelInputPayload(sessionId, request);

        return this.execute(initialState, getStartSignal(), input)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }
}
