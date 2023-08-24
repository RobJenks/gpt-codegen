package org.rj.modelgen.bpmn.execution.signal;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class PrepareLlmRequestSignal extends ModelInterfaceSignal {
    public PrepareLlmRequestSignal(String val) {
        super(PrepareLlmRequestSignal.class); this.val = val;
    }

    @Override
    public String getDescription() {
        return "Prepare new LLM request for submission";
    }

    private String val;
    public String getVal() { return val; }
}
