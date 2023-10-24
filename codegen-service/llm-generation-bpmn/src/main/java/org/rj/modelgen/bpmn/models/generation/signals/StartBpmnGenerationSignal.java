package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.Optional;

public class StartBpmnGenerationSignal extends ModelInterfaceSignal {
    private final String request;
    private final String sessionId;

    public StartBpmnGenerationSignal(String request, String sessionId) {
        super(StartBpmnGenerationSignal.class);
        this.request = request;
        this.sessionId = sessionId;
    }

    public String getRequest() {
        return request;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getDescription() {
        return "Start a new BPMN generation process";
    }
}
