package org.rj.modelgen.bpmn.execution.signal;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class StartBpmnGenerationSignal extends ModelInterfaceSignal {
    public StartBpmnGenerationSignal(String val) {
        super(StartBpmnGenerationSignal.class); this.val = val;
    }

    private String val;
    public String getVal() { return val; }

    @Override
    public String getDescription() {
        return "Start a new BPMN generation process";
    }
}
