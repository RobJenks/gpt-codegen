package org.rj.modelgen.service.context;

import groovy.lang.GroovyShell;
import io.micrometer.common.util.StringUtils;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.rj.modelgen.service.gpt.beans.ContextEntry;
import org.rj.modelgen.service.gpt.beans.PromptContextSubmission;
import org.rj.modelgen.service.gpt.beans.SessionState;
import org.rj.modelgen.service.util.Constants;
import org.rj.modelgen.service.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class GroovyGeneratingContextProvider extends ContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GroovyGeneratingContextProvider.class);
    private static final String REJECT_TOKEN = "NO";

    private final GroovyShell groovyShell;

    public GroovyGeneratingContextProvider() {
        this.groovyShell = new GroovyShell();
    }

    @Override
    public PromptContextSubmission buildBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, this::constrainedPrompt);
    }

    @Override
    public PromptContextSubmission buildUndecoratedBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, Function.identity());
    }

    public PromptContextSubmission buildBodyWithDecoration(SessionState session, String prompt, Function<String, String> decorator) {
        final var context = session.hasLastResponse() ?
                withContinuationContext(session, decorator.apply(prompt)) :
                withInitialContext(decorator.apply(prompt));

        final var body = PromptContextSubmission.defaultConfig(context);
        LOG.info("Request body: {}", Util.serializeOrThrow(body));

        return body;
    }



    private List<ContextEntry> withInitialContext(String prompt) {
        return List.of(
                ContextEntry.forAssistant("def run() {\n    return {};\n}"),
                ContextEntry.forUser(prompt)
        );
    }

    private List<ContextEntry> withContinuationContext(SessionState session, String prompt) {
        String lastValidCode = null;
        for (int i = session.getEvents().size() - 1; i >= 0; --i) {
            final var event = session.getEvents().get(i);

            if (!event.getRole().equals(Constants.ROLE_ASSISTANT)) continue;
            if (event.getContent().equals(REJECT_TOKEN)) continue;

            lastValidCode = event.getContent();
            break;
        }

        if (lastValidCode == null) {
            return withInitialContext(prompt);
        }

        return List.of(
                ContextEntry.forAssistant(lastValidCode),
                ContextEntry.forUser(prompt)
        );
    }

    private String constrainedPrompt(String prompt) {
        return String.format("Make this change to the code.  Ensure your response is valid Groovy code.  Return the full code and ONLY the code. " +
                "If this is not a valid request to change the code, simply return '%s' instead.  The change is: %s", REJECT_TOKEN, prompt);
    }

    @Override
    public List<String> validateResponse(String response) {
        if (StringUtils.isBlank(response)) return List.of("Empty prompt");

        // Avoid conflict with reserved groovy shell root method
        response = response.replace("def run(", "def _run(");

        try {
            final var script = groovyShell.parse(response);
            if (script == null) {
                return List.of("Failed to parse script; it may be invalid");
            }

            return List.of();
        }
        catch (MultipleCompilationErrorsException ex) {
            final var msg = ex.getMessage();
            return List.of(msg
                    .replace("startup failed:", "")
                    .trim());
        }
        catch (Exception ex) {
            return List.of(ex.getMessage());
        }
    }

    @Override
    public String getValidationFailureRetryPrompt(String responseFailingValidation, List<String> validationErrors) {
        return "Your response does not compile.  Ensure it compiles as valid Groovy code.  Return the full code and ONLY the code";
    }

    @Override
    public String sanitizeResponse(String response) {
        if (StringUtils.isBlank(response)) return response;

        // If the model has ignored the "only the code" constraint, check for typical ``` script bounds and crop the response within them
        final var codeBlockOpen = response.indexOf("```");
        if (codeBlockOpen >= 0) {
            final var codeBlockClose = response.lastIndexOf("```");
            if (codeBlockClose > (codeBlockOpen + 3)) {
                // We have open/close block syntax.  Extract just the contents
                response = response.substring(codeBlockOpen + 3, codeBlockClose);
            }
        }

        return response;
    }

    @Override
    public String generateTransformedOutput(String response) {
        return response;    // No transformation required
    }
}
