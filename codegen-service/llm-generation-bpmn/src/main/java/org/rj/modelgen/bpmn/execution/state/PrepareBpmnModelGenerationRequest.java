package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.bpmn.execution.signal.RequestSubmissionToLlmSignal;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class PrepareBpmnModelGenerationRequest extends ModelInterfaceState<PrepareLlmRequestSignal> {
    public PrepareBpmnModelGenerationRequest() {
        super(PrepareBpmnModelGenerationRequest.class);
    }

    @Override
    public String getDescription() {
        return "Prepare BPMN model generation request";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        return Mono.just(new RequestSubmissionToLlmSignal(input.getVal() + ", Prepare request"));
    }
}
