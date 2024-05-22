package org.rj.modelgen.bpmn.models.generation.base;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModelOptions;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.bpmn.models.generation.base.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.base.data.BpmnGenerationModelInputPayload;
import org.rj.modelgen.bpmn.models.generation.base.signals.*;
import org.rj.modelgen.bpmn.models.generation.base.states.*;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.*;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.List;

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
        final var stateValidateLlmResponse = new ValidateBpmnLlmIntermediateModelResponse(modelSchema, modelClass);
        final var stateGenerateBpmnXml = new GenerateBpmnFromIntermediateModel();
        final var stateValidateBpmnModelCorrectness = new ValidateBpmnModelCorrectness();
        final var stateComplete = new BpmnGenerationComplete();

        final var states = List.of(stateInit, statePrepareRequest, stateSubmitToLlm, stateValidateLlmResponse,
                                   stateGenerateBpmnXml, stateValidateBpmnModelCorrectness, stateComplete);

        // Define transition rules between states
        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule(stateInit, BpmnGenerationSignals.PrepareLlmRequest, statePrepareRequest),
                new ModelInterfaceTransitionRule(statePrepareRequest, BpmnGenerationSignals.SubmitRequestToLlm, stateSubmitToLlm),
                new ModelInterfaceTransitionRule(stateSubmitToLlm, BpmnGenerationSignals.ValidateLlmResponse, stateValidateLlmResponse),
                new ModelInterfaceTransitionRule(stateValidateLlmResponse, BpmnGenerationSignals.GenerateBpmnXmlFromLlmResponse, stateGenerateBpmnXml),
                new ModelInterfaceTransitionRule(stateGenerateBpmnXml, BpmnGenerationSignals.ValidateBpmnXml, stateValidateBpmnModelCorrectness),
                new ModelInterfaceTransitionRule(stateValidateBpmnModelCorrectness, BpmnGenerationSignals.CompleteGeneration, stateComplete)
        ));

        return new BpmnGenerationExecutionModel(modelInterface, states, rules);
    }

    private BpmnGenerationExecutionModel(ModelInterface modelInterface, List<ModelInterfaceState> states,
                                         ModelInterfaceTransitionRules rules) {
        super(modelInterface, states, rules);
    }

    public Mono<BpmnGenerationResult> executeModel(String sessionId, String request) {
        final var initialState = ModelInterfaceState.defaultStateId(StartBpmnGeneration.class);

        final var input = new BpmnGenerationModelInputPayload(sessionId, request);
        input.setLlm("gpt-4");
        input.setTemperature(0.7f);

        return this.execute(initialState, BpmnGenerationSignals.StartBpmnGeneration, input)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }
}
