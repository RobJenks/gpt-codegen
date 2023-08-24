package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.bpmn.execution.signal.StartBpmnGenerationSignal;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class StartBpmnGeneration extends ModelInterfaceState<StartBpmnGenerationSignal> {
    public StartBpmnGeneration() {
        super(StartBpmnGeneration.class);
    }

    @Override
    public String getDescription() {
        return "Begin BPMN Generation";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        return Mono.just(new PrepareLlmRequestSignal(input.getVal() + ", Start gen"));
    }
}
