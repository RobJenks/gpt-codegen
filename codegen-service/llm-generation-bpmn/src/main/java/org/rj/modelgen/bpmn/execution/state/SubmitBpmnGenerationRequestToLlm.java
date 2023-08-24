package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.bpmn.execution.signal.RequestSubmissionToLlmSignal;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class SubmitBpmnGenerationRequestToLlm extends ModelInterfaceState<RequestSubmissionToLlmSignal> {
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

        return Mono.just(new PrepareLlmRequestSignal(input.getVal() + ", Submit request"));
    }
}
