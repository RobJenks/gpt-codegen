package org.rj.modelgen.bpmn.execution.signal;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class RequestSubmissionToLlmSignal extends ModelInterfaceSignal {
    public RequestSubmissionToLlmSignal(String val) {
        super(RequestSubmissionToLlmSignal.class);this.val = val;
    }

    @Override
    public String getDescription() {
        return "Request submission to LLM";
    }

    private String val;
    public String getVal() { return val; }
}
