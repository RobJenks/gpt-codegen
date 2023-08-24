package org.rj.modelgen.bpmn.execution.signal;

import org.rj.modelgen.bpmn.execution.state.SubmitBpmnGenerationRequestToLlm;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class RequestSubmissionToLlmSignal extends ModelInterfaceSignal<SubmitBpmnGenerationRequestToLlm> {
    public static final String ID = RequestSubmissionToLlmSignal.class.getName();
    public RequestSubmissionToLlmSignal() {
        super(ID);
    }

    @Override
    public String getDescription() {
        return "Request submission to LLM";
    }
}
