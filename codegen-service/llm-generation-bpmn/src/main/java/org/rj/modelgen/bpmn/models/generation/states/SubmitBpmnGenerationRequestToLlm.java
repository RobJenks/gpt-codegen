package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class SubmitBpmnGenerationRequestToLlm extends ModelInterfaceState<LlmModelRequestPreparedSuccessfully> {
    public SubmitBpmnGenerationRequestToLlm() {
        super(SubmitBpmnGenerationRequestToLlm.class);
    }

    @Override
    public String getDescription() {
        return "Submit new BPMN generation request to LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        // TODO: 1
        return Mono.just(new LlmResponseReceived(input.getModelRequest() + " // RESPONSE"));
    }
}
