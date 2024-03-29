package org.rj.modelgen.llm.context.provider.impl;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.LegacyContextProvider;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.session.SessionState;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LegacyDefaultContextShorteningProvider extends LegacyContextProvider {
    private final Logger LOG = LoggerFactory.getLogger(LegacyDefaultContextShorteningProvider.class);

    @Override
    public ModelRequest buildBody(SessionState session, String prompt) {
        final var context = session.getContext().hasLatestModelEntry() ?
                continuationContext(session, prompt) :
                newContext(prompt);

        final var body = new ModelRequest("gpt-4", 0.7f, new Context(context));
        LOG.info("Request body: {}", Util.serializeOrThrow(body));

        return body;
    }

    @Override
    public ModelRequest buildUndecoratedBody(SessionState session, String prompt) {
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
