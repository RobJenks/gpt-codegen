package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.request.ModelRequest;
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
        if (input.getContext() == null) throw new BpmnGenerationException("No valid context for LLM submission");

        final var request = new ModelRequest("gpt-4", 0.7f, input.getContext());
        return getModelInterface().submit(input.getSessionId(), request)
                .map(response -> new LlmResponseReceived(input.getSessionId(), response));
                //.onErrorResume(t -> new );
    }
}
