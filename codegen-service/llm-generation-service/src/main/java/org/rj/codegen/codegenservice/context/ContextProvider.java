package org.rj.codegen.codegenservice.context;

import org.rj.codegen.codegenservice.gpt.beans.ContextEntry;
import org.rj.codegen.codegenservice.gpt.beans.PromptContextSubmission;
import org.rj.codegen.codegenservice.gpt.beans.SessionState;
import org.rj.codegen.codegenservice.gpt.beans.SubmissionResponse;

import java.util.List;

public abstract class ContextProvider {

    /** Implemented by subclasses **/
    public abstract PromptContextSubmission buildBody(SessionState session, String prompt);
    public abstract PromptContextSubmission buildUndecoratedBody(SessionState session, String prompt);
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
