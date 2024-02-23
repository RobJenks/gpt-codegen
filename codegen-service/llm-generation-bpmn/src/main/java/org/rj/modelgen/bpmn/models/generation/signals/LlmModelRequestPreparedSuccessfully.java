package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class LlmModelRequestPreparedSuccessfully extends ModelInterfaceSignal {
    private final String sessionId;
    private final Context context;

    public LlmModelRequestPreparedSuccessfully(String sessionId, Context context) {
        super(BpmnGenerationSignals.SubmitRequestToLlm);
        this.sessionId = sessionId;
        this.context = context;
    }

    @Override
    public String getDescription() {
        return "Request for submission to LLM prepared successfully";
    }

    public String getSessionId() {
        return sessionId;
    }

    public Context getContext() {
        return context;
    }
}
