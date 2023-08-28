package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class NewBpmnGenerationRequestReceived extends ModelInterfaceSignal {
    private final String currentIL;
    private final String request;

    public NewBpmnGenerationRequestReceived(String currentIL, String request) {
        super(NewBpmnGenerationRequestReceived.class);
        this.currentIL = currentIL;
        this.request = request;
    }

    @Override
    public String getDescription() {
        return "New request for BPMN generation was received";
    }

    public String getCurrentIL() {
        return currentIL;
    }

    public String getRequest() {
        return request;
    }
}
