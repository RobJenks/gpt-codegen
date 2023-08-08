package org.rj.codegen.codegenservice.context;

import org.rj.codegen.codegenservice.gpt.beans.PromptContextSubmission;
import org.rj.codegen.codegenservice.gpt.beans.SessionState;
import org.rj.codegen.codegenservice.gpt.beans.SubmissionResponse;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultContextShorteningProvider extends ContextProvider {
    private final Logger LOG = LoggerFactory.getLogger(DefaultContextShorteningProvider.class);

    @Override
    public PromptContextSubmission buildBody(SessionState session, String prompt) {
        final var context = session.hasLastResponse() ?
                continuationContext(session, prompt) :
                newContext(prompt);

        final var body = PromptContextSubmission.defaultConfig(context);
        LOG.info("Request body: {}", Util.serializeOrThrow(body));

        return body;
    }

    @Override
    public PromptContextSubmission buildUndecoratedBody(SessionState session, String prompt) {
        return buildBody(session, prompt);
    }

    @Override
    public List<String> validateResponse(String response) {
        return List.of();
    }

    @Override
    public String getValidationFailureRetryPrompt(String responseFailingValidation, List<String> validationErrors) {
        throw new IllegalStateException("Default context provider does not support retry on validation failures");
    }

    @Override
    public String sanitizeResponse(String response) {
        return response;
    }

    @Override
    public String generateTransformedOutput(String response) {
        return response;
    }
}
