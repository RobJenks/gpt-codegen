package org.rj.modelgen.bpmn.models.generation;

import org.rj.modelgen.bpmn.intrep.bpmn.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.signals.*;
import org.rj.modelgen.bpmn.models.generation.states.*;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.*;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class BpmnGenerationExecutionModel extends ModelInterfaceStateMachine {
    public static BpmnGenerationExecutionModel create(ModelInterface modelInterface, ModelSchema modelSchema,
                                                      BpmnGenerationExecutionModelOptions options) {
        final var modelClass = BpmnIntermediateModel.class;

        final var generationPrompt = options.shouldUseHistory()
                ? Util.loadStringResource("content/bpmn-prompt-template")
                : Util.loadStringResource("content/bpmn-prompt-template-no-history");

        final var promptGenerator = BpmnGenerationPromptGenerator.create(
                generationPrompt,
                "<not-implemented>",
                "<not-implemented>"
        );

        // Build model states
        final var stateInit = new StartBpmnGeneration();
        final var statePrepareRequest = new PrepareBpmnModelGenerationRequest(modelSchema, promptGenerator);
        final var stateSubmitToLlm = new SubmitBpmnGenerationRequestToLlm();
        final var stateValidateLlmResponse = new ValidateLlmIntermediateModelResponse(modelSchema, modelClass);
        final var stateGenerateBpmnXml = new GenerateBpmnFromIntermediateModel();
        final var stateValidateBpmnModelCorrectness = new ValidateBpmnModelCorrectness();
        final var stateComplete = new BpmnGenerationComplete();

        final var states = List.of(stateInit, statePrepareRequest, stateSubmitToLlm, stateValidateLlmResponse,
                                   stateGenerateBpmnXml, stateValidateBpmnModelCorrectness, stateComplete);

        // Define transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule<>(stateInit, NewBpmnGenerationRequestReceived.class, statePrepareRequest),
                new ModelInterfaceTransitionRule<>(statePrepareRequest, LlmModelRequestPreparedSuccessfully.class, stateSubmitToLlm),
                new ModelInterfaceTransitionRule<>(stateSubmitToLlm, LlmResponseReceived.class, stateValidateLlmResponse),
                new ModelInterfaceTransitionRule<>(stateValidateLlmResponse, LlmResponseModelDataIsValid.class, stateGenerateBpmnXml),
                new ModelInterfaceTransitionRule<>(stateGenerateBpmnXml, BpmnXmlSuccessfullyGeneratedFromModelResponse.class, stateValidateBpmnModelCorrectness),
                new ModelInterfaceTransitionRule<>(stateValidateBpmnModelCorrectness, BpmnXmlDataPassedValidation.class, stateComplete)
        ));

        return new BpmnGenerationExecutionModel(modelInterface, states, rules);
    }

    private BpmnGenerationExecutionModel(ModelInterface modelInterface, List<ModelInterfaceState<? extends ModelInterfaceSignal>> states,
                                         ModelInterfaceTransitionRules rules) {
        super(modelInterface, states, rules);
    }

    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request) {
        return executeModel(sessionId, request, null);
    }

    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request, Map<String, Object> metadata) {
        final var initialState = ModelInterfaceState.defaultStateId(StartBpmnGeneration.class);
        final var startSignal = new StartBpmnGenerationSignal(request, sessionId);

        if (metadata != null) {
            startSignal.setMetadata(metadata);
        }

        return this.execute(initialState, startSignal)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }
}
