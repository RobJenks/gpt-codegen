package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class PrepareBpmnModelGenerationRequest extends ModelInterfaceState {
    public static String ID = PrepareBpmnModelGenerationRequest.class.getName();
    public PrepareBpmnModelGenerationRequest() {
        super(ID);
    }

    @Override
    public String getDescription() {
        return "Prepare BPMN model generation request";
    }

    @Override
    protected Mono<ModelInterfaceSignal<? extends ModelInterfaceState>>
    invokeAction(ModelInterfaceSignal<? extends ModelInterfaceState> inputSignal) {
        return null;
    }
}
