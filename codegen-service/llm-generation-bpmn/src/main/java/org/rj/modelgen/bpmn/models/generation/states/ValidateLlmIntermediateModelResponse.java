package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;

public class ValidateLlmIntermediateModelResponse extends ModelInterfaceState<LlmResponseReceived> {
    public ValidateLlmIntermediateModelResponse() {
        super(ValidateLlmIntermediateModelResponse.class);
    }

    @Override
    public String getDescription() {
        return "Clean up and validate intermediate representation returned by LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        // Perform validation

        // TODO: Verify successful.  Record other data on e.g. token usage
        getModelInterface().getOrCreateSession(input.getSessionId())
                .getContext().addModelResponse(input.getModelResponse().getMessage());

        return Mono.just(new LlmResponseModelDataIsValid(input.getSessionId(), input.getModelResponse().getMessage(), List.of("A", "B", "C")));
    }
}
