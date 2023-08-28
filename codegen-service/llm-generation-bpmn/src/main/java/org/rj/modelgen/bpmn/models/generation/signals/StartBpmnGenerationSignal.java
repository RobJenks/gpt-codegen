package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class StartBpmnGenerationSignal extends ModelInterfaceSignal {
    private final String currentIL;
    private final String request;

    public StartBpmnGenerationSignal(String currentIL, String request) {
        super(StartBpmnGenerationSignal.class);
        this.currentIL = currentIL;
        this.request = request;
    }

    public String getCurrentIL() {
        return currentIL;
    }

    public String getRequest() {
        return request;
    }

    @Override
    public String getDescription() {
        return "Start a new BPMN generation process";
    }
}
