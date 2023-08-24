package org.rj.modelgen.bpmn.execution.state;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class SubmitBpmnGenerationRequestToLlm extends ModelInterfaceState {
    public static String ID = SubmitBpmnGenerationRequestToLlm.class.getName();
    public SubmitBpmnGenerationRequestToLlm() {
        super(ID);
    }

    @Override
    public String getDescription() {
        return "Submit new BPMN generation request to LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal<? extends ModelInterfaceState>>
    invokeAction(ModelInterfaceSignal<? extends ModelInterfaceState> inputSignal) {
        return null;
    }
}
