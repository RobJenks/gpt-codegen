package org.rj.codegen.codegenservice.gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.rj.codegen.codegenservice.context.ContextProvider;
import org.rj.codegen.codegenservice.context.DefaultContextShorteningProvider;
import org.rj.codegen.codegenservice.context.GroovyGeneratingContextProvider;
import org.rj.codegen.codegenservice.gpt.beans.*;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@PropertySource("classpath:application.properties")
public class GptService {
    private static final Logger LOG = LoggerFactory.getLogger(GptService.class);

    @Autowired
    private Environment environment;
    @Autowired
    private ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, SessionState> sessions;
    private WebClient client;
    private ContextProvider contextProvider;

    public GptService() {
        sessions = new ConcurrentHashMap<>();
        //contextProvider = new DefaultContextShorteningProvider();
        contextProvider = new GroovyGeneratingContextProvider();
    }

    @PostConstruct
    public void postConstruct() {
        client = buildClient();
    }

    @GetMapping("/api/gpt/session/{id}/state")
    public SessionState getSessionState(
            @PathVariable("id") String id
    ) {
        return sessions.getOrDefault(id, SessionState.NONE);
    }

    @PostMapping("/api/gpt/session/{id}/prompt")
    public Mono<SessionState> prompt(
            @PathVariable("id") String id,
            @RequestBody Prompt prompt
    ) {
        recordPrompt(id, prompt);

        return submitPrompt(id, prompt.getPrompt())
                .map(response -> recordResponse(id, response))
                .flatMap(this::validateAndAttemptResponseCorrection)
                .map(__ -> getSession(id));
    }

    private WebClient buildClient() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + getKey())
                .build();
    }

    private SessionState getSession(String id) {
        final var session = sessions.get(id);
        if (session != null) return session;

        final var newSession = new SessionState(id);
        sessions.put(id, newSession);
        return newSession;
    }

    private Mono<SubmissionResponse> submitPrompt(String sessionId, String prompt) {
        final var session = getSession(sessionId);
        final var body = contextProvider.buildBody(session, prompt);

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

        return client.post()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .body(BodyInserters.fromValue(body))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(SubmissionResponse.class)
                .doOnError(t -> LOG.error("ERROR OCCURRED: " + t))
                .map(ResponseEntity::getBody);
    }

    private void recordPrompt(String sessionId, Prompt prompt) {
        final var session = getSession(sessionId);

        session.addEvent(ContextEntry.forUser(prompt.getPrompt()));
        session.setCurrentTemperature(Optional.ofNullable(prompt.getTemperature()).orElse(0.7f));
    }

    private SessionState recordResponse(String sessionId, SubmissionResponse response) {
        response.getChoices().forEach(choice -> LOG.info("Response received: {}", Util.serializeOrThrow(choice)));

        final var chosenResponse = response.getChoices().stream().findFirst()
                .map(SubmissionResponse.Choice::getMessage)
                .map(ContextEntry::getContent)
                .orElse(null);

        final var finalResponse = contextProvider.sanitizeResponse("```" + chosenResponse + "```");

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
            final var retryPrompt = contextProvider.getValidationFailureRetryPrompt(session.getLastResponse());
            final var body = contextProvider.buildUndecoratedBody(session, retryPrompt);

            return submitPrompt(session.getId(), body)
                    .map(resp -> recordResponse(session.getId(), resp))
                    .map(s -> validateResponse(s.getId(), s.getLastResponse()))
                    .map(nowValid -> {
                        session.setIterationsRequired(session.getIterationsRequired() + 1);
                        session.setValidOutput(nowValid);

                        if (nowValid) {
                            session.setValidationErrors(List.of("Output was regenerated due to validation failures and is now valid"));
                        }
                        else {
                            session.addValidationError("Attempted regeneration of output but could not resolve validation failures");
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
        final var validationErrors = contextProvider.validateResponse(response);

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

    private String getKey() {
        return Optional.ofNullable(environment)
                .map(env -> env.getProperty("token"))
                .map(k -> getClass().getClassLoader().getResource(k))
                .map(url -> new File(url.getFile()))
                .map(file -> {
                    try {
                        return Files.readString(file.toPath());
                    }
                    catch (Exception ex) {
                        throw new RuntimeException("Failed to load token from file: " + ex.getMessage(), ex);
                    }})
                .orElseThrow(() -> new RuntimeException("Failed to load token"));
    };

}
