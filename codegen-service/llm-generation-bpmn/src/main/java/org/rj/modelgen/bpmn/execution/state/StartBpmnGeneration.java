package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class StartBpmnGeneration extends ModelInterfaceState {
    public static final String ID = StartBpmnGeneration.class.getName();
    public StartBpmnGeneration() {
        super(ID);
    }

    @Override
    public String getDescription() {
        return "Begin BPMN Generation";
    }

    @Override
    protected Mono<ModelInterfaceSignal<? extends ModelInterfaceState>> invokeAction(ModelInterfaceSignal<? extends ModelInterfaceState> inputSignal) {
        return Mono.just(new PrepareLlmRequestSignal());
    }
}
