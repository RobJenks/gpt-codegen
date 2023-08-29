package org.rj.modelgen.bpmn.models.generation;

import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.signals.*;
import org.rj.modelgen.bpmn.models.generation.states.*;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.*;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.List;

public class BpmnGenerationExecutionModel extends ModelInterfaceStateMachine {
    public static BpmnGenerationExecutionModel create(ModelSchema modelSchema) {
        final var promptGenerator = BpmnGenerationPromptGenerator.create(
                Util.loadStringResource("content/bpmn-prompt-template"),
                "<not-implemented>",
                "<not-implemented>"
        );

        // Build model states
        final var stateInit = new StartBpmnGeneration();
        final var statePrepareRequest = new PrepareBpmnModelGenerationRequest(modelSchema, promptGenerator);
        final var stateSubmitToLlm = new SubmitBpmnGenerationRequestToLlm();
        final var stateValidateLlmResponse = new ValidateLlmIntermediateModelResponse();
        final var stateGenerateBpmnXml = new GenerateBpmnXmlFromIntermediateRepresentation();
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

        return new BpmnGenerationExecutionModel(states, rules);
    }

    private BpmnGenerationExecutionModel(List<ModelInterfaceState<? extends ModelInterfaceSignal>> states, ModelInterfaceTransitionRules rules) {
        super(states, rules);
    }

    public Mono<BpmnGenerationResult> executeModel(String currentIL, String request) {
        final var initialState = ModelInterfaceState.defaultStateId(StartBpmnGeneration.class);
        final var startSignal = new StartBpmnGenerationSignal(currentIL, request);

        return this.execute(initialState, startSignal)
                .map(BpmnGenerationResult::fromModelExecutionResult);
    }

    public static void main(String[] args) {
        final var model = BpmnGenerationExecutionModel.create();
        model.executeModel("currentILData", "Do something to the model")
                .subscribe(result -> {
                    System.out.println("Result.success = " + result.isSuccessful());
                    System.out.println("Result.generated = " + result.getGeneratedBpmn());
                    System.out.println("Result.modelValidation = " + String.join(", ", result.getModelValidationMessages()));
                    System.out.println("Result.bpmnValidation = " + String.join(", ", result.getBpmnValidationMessages()));
                });
    }
}
