package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import reactor.core.publisher.Mono;

public class StartBpmnGeneration extends ModelInterfaceState {
    public StartBpmnGeneration() {
        super(StartBpmnGeneration.class);
    }

    @Override
    public String getDescription() {
        return "Begin BPMN Generation";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {
        if (!getPayload().hasData(StandardModelData.Request)) return error("Generation request is missing input request data");

        final String sessionId = getPayload().getOrThrow(StandardModelData.SessionId, () -> new BpmnGenerationException("No valid session ID"));
        final var session = getModelInterface().getOrCreateSession(sessionId);

        return outboundSignal(BpmnGenerationSignals.PrepareLlmRequest)
                .withPayloadData(StandardModelData.SessionId, session.getId())
                .withPayloadData(StandardModelData.Context, session.getContext())
                .mono();
    }
}
