package org.rj.modelgen.llm.context.provider;

import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.session.SessionState;

import java.util.List;

public abstract class ContextProvider {

    /** Implemented by subclasses **/
    public abstract ModelRequest buildBody(SessionState session, String prompt);
    public abstract ModelRequest buildUndecoratedBody(SessionState session, String prompt);
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
                ContextEntry.forModel(session.getLastResponse()),
                ContextEntry.forUser(prompt)
        );
    }
}
