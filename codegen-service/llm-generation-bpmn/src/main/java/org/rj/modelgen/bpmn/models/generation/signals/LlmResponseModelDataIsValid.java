package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class LlmResponseModelDataIsValid extends ModelInterfaceSignal {
    private final String sessionId;
    private final ModelResponse modelResponse;
    private final String sanitizedResponseContent;
    private final List<String> validationMessages;

    public LlmResponseModelDataIsValid(String sessionId, ModelResponse modelResponse, String sanitizedResponseContent, List<String> validationMessages) {
        super(LlmResponseModelDataIsValid.class);
        this.sessionId = sessionId;
        this.modelResponse = modelResponse;
        this.sanitizedResponseContent = sanitizedResponseContent;
        this.validationMessages = validationMessages;
    }

    @Override
    public String getDescription() {
        return "Model data returned in the LLM response is valid";
    }

    public String getSessionId() {
        return sessionId;
    }

    public ModelResponse getModelResponse() {
        return modelResponse;
    }

    public String getSanitizedResponseContent() {
        return sanitizedResponseContent;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }
}
