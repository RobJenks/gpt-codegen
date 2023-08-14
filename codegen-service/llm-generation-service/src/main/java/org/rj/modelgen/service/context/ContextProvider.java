package org.rj.modelgen.service.context;

import org.rj.modelgen.llm.beans.ContextEntry;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelRequest;
import org.rj.modelgen.llm.beans.SessionState;

import java.util.List;

public abstract class ContextProvider {

    /** Implemented by subclasses **/
    public abstract OpenAIModelRequest buildBody(SessionState session, String prompt);
    public abstract OpenAIModelRequest buildUndecoratedBody(SessionState session, String prompt);
    public abstract List<String> validateResponse(String response);
    public abstract String getValidationFailureRetryPrompt(String responseFailingValidation, List<String> validationErrors);
    public abstract String sanitizeResponse(String response);

    public abstract String generateTransformedOutput(String response);

    protected List<ContextEntry> newContext(String prompt) {
        return List.of(
                ContextEntry.forUser(prompt)
        );
    }

    protected List<ContextEntry> continuationContext(SessionState session, String prompt) {
        return List.of(
                ContextEntry.forAssistant(session.getLastResponse()),
                ContextEntry.forUser(prompt)
        );
    }

}
