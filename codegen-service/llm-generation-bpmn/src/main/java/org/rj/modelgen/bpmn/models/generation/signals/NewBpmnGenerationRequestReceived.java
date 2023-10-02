package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class NewBpmnGenerationRequestReceived extends ModelInterfaceSignal {
    private final Context currentContext;
    private final String request;

    public NewBpmnGenerationRequestReceived(Context currentContext, String request) {
        super(NewBpmnGenerationRequestReceived.class);
        this.currentContext = currentContext;
        this.request = request;
    }

    @Override
    public String getDescription() {
        return "New request for BPMN generation was received";
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public String getRequest() {
        return request;
    }
}
