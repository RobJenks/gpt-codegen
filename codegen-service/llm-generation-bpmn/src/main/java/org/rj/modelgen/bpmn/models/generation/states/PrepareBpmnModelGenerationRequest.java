package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class PrepareBpmnModelGenerationRequest extends ModelInterfaceState<NewBpmnGenerationRequestReceived> {
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

        // TODO: 1
        return Mono.just(new LlmModelRequestPreparedSuccessfully(input.getCurrentIL() + "," + input.getRequest()));
    }
}
