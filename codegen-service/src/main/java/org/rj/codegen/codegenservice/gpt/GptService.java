package org.rj.codegen.codegenservice.gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.rj.codegen.codegenservice.context.BpmnGeneratingContextProvider;
import org.rj.codegen.codegenservice.context.ContextProvider;
import org.rj.codegen.codegenservice.context.GroovyGeneratingContextProvider;
import org.rj.codegen.codegenservice.gpt.beans.*;
import org.rj.codegen.codegenservice.gpt.client.GptClient;
import org.rj.codegen.codegenservice.gpt.client.GptClientImpl;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@PropertySource("classpath:application.properties")
public class GptService {
    private static final Logger LOG = LoggerFactory.getLogger(GptService.class);

    @Autowired
    private Environment environment;
    @Autowired
    private ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, SessionState> sessions;
    private GptClient client;
    private final Map<ExecutionContext, ContextProvider> contextProviders;

    public GptService() {
        sessions = new ConcurrentHashMap<>();

        contextProviders = Map.of(
                ExecutionContext.Groovy, new GroovyGeneratingContextProvider(),
                ExecutionContext.BPMN, new BpmnGeneratingContextProvider());
    }

    @PostConstruct
    public void postConstruct() {
        client = GptClient.build(environment);
    }

    @GetMapping("/api/gpt/session/{id}/state")
    public SessionState getSessionState(
            @PathVariable("id") String id
    ) {
        return sessions.getOrDefault(id, SessionState.NONE);
    }

    @PostMapping("/api/gpt/session/{id}/prompt/groovy")
    public Mono<SessionState> promptGroovy(
            @PathVariable("id") String id,
            @RequestBody Prompt prompt
    ) {
        getOrCreateSession(id, ExecutionContext.Groovy);
        recordPrompt(id, prompt);

        return submitPrompt(id, prompt.getPrompt())
                .map(response -> recordResponse(id, response))
                .flatMap(this::validateAndAttemptResponseCorrection)
                .map(__ -> getSession(id));
    }

    @PostMapping("/api/gpt/session/{id}/prompt/bpmn")
    public Mono<SessionState> promptBpmn(
            @PathVariable("id") String id,
            @RequestBody Prompt prompt
    ) {
        getOrCreateSession(id, ExecutionContext.BPMN);
        recordPrompt(id, prompt);

        return submitPrompt(id, prompt.getPrompt())
                .map(response -> recordResponse(id, response))
                .flatMap(this::validateAndAttemptResponseCorrection)
                .doOnSuccess(this::updateBpmnContentIfValid)
                .map(__ -> getSession(id));
    }

    private SessionState getSession(String id) {
        final var session = sessions.get(id);
        if (session == null) throw new RuntimeException("No session with ID: " + id);

        return session;
    }

    private SessionState getOrCreateSession(String id, ExecutionContext executionContext) {
        final var session = sessions.get(id);
        if (session != null) {
            if (session.getExecutionContext() != executionContext) {
                throw new RuntimeException(String.format("Unexpected execution context '%s' for session '%s' (expecting '%s')",
                        executionContext, id, session.getExecutionContext()));
            }
            return session;
        }

        final var newSession = new SessionState(id, executionContext);
        sessions.put(id, newSession);
        return newSession;
    }

    private ContextProvider getContextProvider(String sessionId) {
        if (StringUtils.isBlank(sessionId)) throw new RuntimeException("Cannot get execution context for missing session ID");

        final var session = getSession(sessionId);
        final var executionContext = contextProviders.get(session.getExecutionContext());
        if (executionContext == null) {
            throw new RuntimeException(String.format("Cannot get execution context for session '%s'; unrecognized context type '%s'", sessionId, session.getExecutionContext()));
        }

        return executionContext;
    }

    private Mono<SubmissionResponse> submitPrompt(String sessionId, String prompt) {
        final var session = getSession(sessionId);
        final var body = getContextProvider(sessionId).buildBody(session, prompt);

        return submitPrompt(sessionId, body);
    }

    private Mono<SubmissionResponse> submitPrompt(String sessionId, PromptContextSubmission body) {
        final var session = getSession(sessionId);

        session.addEstimatedTokensForPrompt(body);

        session.setLastPrompt(Optional.ofNullable(body.getMessages())
                .map(msgs -> msgs.get(msgs.size() - 1))
                .map(ContextEntry::getContent)
                .orElse(null));

        // Insert temperature here based upon session state, so that it is applied whichever method we used to generate a new prompt
        body.setTemperature(session.getCurrentTemperature());

        return client.submit(body);
    }

    private void recordPrompt(String sessionId, Prompt prompt) {
        final var session = getSession(sessionId);

        session.addEvent(ContextEntry.forUser(prompt.getPrompt()));
        session.setCurrentTemperature(Optional.ofNullable(prompt.getTemperature()).orElse(0.7f));
    }

    private SessionState recordResponse(String sessionId, SubmissionResponse response) {
        LOG.info("Response received: {}", Util.serializeOrThrow(response));

        final var chosenResponse = response.getChoices().stream().findFirst()
                .map(SubmissionResponse.Choice::getMessage)
                .map(ContextEntry::getContent)
                .orElse(null);

        final var finalResponse = getContextProvider(sessionId).sanitizeResponse("```" + chosenResponse + "```");

        final var session = getSession(sessionId);

        session.setLastResponse(finalResponse);

        session.getEvents().addAll(response.getChoices().stream()
                .map(SubmissionResponse.Choice::getMessage)
                .toList());

        session.setIterationsRequired(1);

        session.addTotalTokensUsed(response.getUsage().getTotal_tokens());
        session.addEstimatedTokensForResponse(response);

        return session;
    }

    private Mono<SessionState> validateAndAttemptResponseCorrection(SessionState session) {
        final var valid = validateResponse(session.getId(), session.getLastResponse());

        if (!valid) {
            final var contextProvider = getContextProvider(session.getId());
            final var retryPrompt = contextProvider.getValidationFailureRetryPrompt(session.getLastResponse(), session.getValidationErrors());
            final var body = contextProvider.buildUndecoratedBody(session, retryPrompt);

            return submitPrompt(session.getId(), body)
                    .map(resp -> recordResponse(session.getId(), resp))
                    .map(s -> validateResponse(s.getId(), s.getLastResponse()))
                    .map(nowValid -> {
                        session.setIterationsRequired(session.getIterationsRequired() + 1);
                        session.setValidOutput(nowValid);

                        final var previousValidationErrors = Optional.ofNullable(session.getValidationErrors()).orElseGet(List::of)
                                .stream().collect(Collectors.joining(",", "[", "]"));

                        if (nowValid) {
                            session.setValidationErrors(List.of(String.format("Output was regenerated due to validation failures and is now valid.  Previous failures: %s", previousValidationErrors)));
                        }
                        else {
                            session.addValidationError(String.format("Attempted regeneration of output but could not resolve validation failures: %s", previousValidationErrors));
                        }

                        return session;
                    });
        }
        else {
            return Mono.just(session);
        }
    }

    private boolean validateResponse(String sessionId, String response) {
        final var session = getSession(sessionId);
        final var validationErrors = getContextProvider(sessionId).validateResponse(response);

        if (validationErrors.isEmpty()) {
            session.setValidOutput(true);
            session.setValidationErrors(List.of());
        }
        else {
            session.setValidOutput(false);
            session.setValidationErrors(validationErrors);
        }

        return session.getValidOutput();
    }

    private void updateBpmnContentIfValid(SessionState session) {
        if (session == null) return;

        if (session.hasValidationErrors()) {
            LOG.info("Not regenerating BPMN data for session '{}' since latest response has failed validation", session.getId());
            return;
        }

        final var modelContent = getContextProvider(session.getId()).generateTransformedOutput(session.getLastResponse());
        session.setTransformedContent(modelContent);
    }
}
