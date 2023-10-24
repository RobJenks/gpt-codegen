package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class LlmResponseReceived extends ModelInterfaceSignal {
    private final String sessionId;
    private final ModelResponse modelResponse;

    public LlmResponseReceived(String sessionId, ModelResponse modelResponse) {
        super(LlmResponseReceived.class);
        this.sessionId = sessionId;
        this.modelResponse = modelResponse;
    }

    @Override
    public String getDescription() {
        return "Valid response returned by LLM";
    }

    public String getSessionId() {
        return sessionId;
    }

    public ModelResponse getModelResponse() {
        return modelResponse;
    }
}
