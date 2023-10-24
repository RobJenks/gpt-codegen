package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.function.Supplier;

public class NewBpmnGenerationRequestReceived extends ModelInterfaceSignal {
    private final String sessionId;
    private final Context currentContext;
    private final String request;

    public NewBpmnGenerationRequestReceived(String sessionId, Context currentContext, String request) {
        super(NewBpmnGenerationRequestReceived.class);
        this.sessionId = sessionId;
        this.currentContext = currentContext;
        this.request = request;
    }

    @Override
    public String getDescription() {
        return "New request for BPMN generation was received";
    }

    public String getSessionId() {
        return sessionId;
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public String getRequest() {
        return request;
    }
}
