package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.StartBpmnGenerationSignal;
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

        if (input.getCurrentIL() == null) return error("Generation request is missing current IL data");
        if (input.getRequest() == null) return error("Generation request is missing model request data");

        return Mono.just(new NewBpmnGenerationRequestReceived(input.getCurrentIL(), input.getRequest()));
    }
}
