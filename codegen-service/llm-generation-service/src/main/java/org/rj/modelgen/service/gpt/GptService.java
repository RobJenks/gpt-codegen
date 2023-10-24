//package org.rj.modelgen.service.gpt;
//
//import jakarta.annotation.PostConstruct;
//import org.apache.commons.lang3.StringUtils;
//import org.rj.modelgen.llm.client.LlmClient;
//import org.rj.modelgen.llm.client.LlmClientImpl;
//import org.rj.modelgen.llm.client.LlmMockClientImpl;
//import org.rj.modelgen.llm.context.ContextEntry;
//import org.rj.modelgen.llm.integrations.openai.OpenAIClientConfig;
//import org.rj.modelgen.llm.request.ModelRequest;
//import org.rj.modelgen.bpmn.context.__BpmnGeneratingContextProvider;
//import org.rj.modelgen.llm.context.provider.__ContextProvider;
//import org.rj.modelgen.llm.response.ModelResponse;
//import org.rj.modelgen.groovy.context.GroovyGeneratingContextProvider;
//import org.rj.modelgen.llm.beans.*;
//import org.rj.modelgen.llm.session.SessionState;
//import org.rj.modelgen.llm.util.Constants;
//import org.rj.modelgen.llm.util.Util;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.core.env.Environment;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@RestController
//@PropertySource("classpath:application.properties")
//public class GptService {
//    private static final Logger LOG = LoggerFactory.getLogger(GptService.class);
//    private static final boolean PERFORM_AUTO_RESUBMISSION_ON_VALIDATION_FAILURE = false;
//    private static final boolean GENERATE_ON_VALIDATION_FAILURE = true;
//
//    @Autowired
//    private Environment environment;
//    private final ConcurrentHashMap<String, SessionState> sessions;
//    private LlmClient client;
//    private final Map<ExecutionContext, __ContextProvider> contextProviders;
//
//    public GptService() {
//        sessions = new ConcurrentHashMap<>();
//
//        contextProviders = Map.of(
//                ExecutionContext.Groovy, new GroovyGeneratingContextProvider(),
//                ExecutionContext.BPMN, new __BpmnGeneratingContextProvider());
//    }
//
//    @PostConstruct
//    public void postConstruct() {
//        OpenAIClientConfig config = new OpenAIClientConfig(this::getKey);
//        client = new LlmClientImpl<>(config);
//    }
//
//    @GetMapping("/api/gpt/session/{id}/state")
//    public SessionState getSessionState(
//            @PathVariable("id") String id
//    ) {
//        return sessions.getOrDefault(id, SessionState.NONE);
//    }
//
//    @PostMapping("/api/gpt/session/{id}/prompt/groovy")
//    public Mono<SessionState> promptGroovy(
//            @PathVariable("id") String id,
//            @RequestBody Prompt prompt
//    ) {
//        getOrCreateSession(id, ExecutionContext.Groovy);
//        recordPrompt(id, prompt);
//
//        return submitPrompt(id, prompt.getPrompt())
//                .map(response -> recordResponse(id, response))
//                .flatMap(this::validateAndAttemptResponseCorrection)
//                .map(__ -> getSession(id));
//    }
//
//    @PostMapping("/api/gpt/session/{id}/prompt/bpmn")
//    public Mono<SessionState> promptBpmn(
//            @PathVariable("id") String id,
//            @RequestBody Prompt prompt
//    ) {
//        getOrCreateSession(id, ExecutionContext.BPMN);
//        recordPrompt(id, prompt);
//
//        return submitPrompt(id, prompt.getPrompt())
//                .map(response -> recordResponse(id, response))
//                .flatMap(this::validateAndAttemptResponseCorrection)
//                .doOnSuccess(this::updateBpmnContentIfValid)
//                .map(__ -> getSession(id));
//    }
//
//    private SessionState getSession(String id) {
//        final var session = sessions.get(id);
//        if (session == null) throw new RuntimeException("No session with ID: " + id);
//
//        return session;
//    }
//
//    private SessionState getOrCreateSession(String id, ExecutionContext executionContext) {
//        final var session = sessions.get(id);
//        if (session != null) {
//            if (session.getExecutionContext() != executionContext) {
//                throw new RuntimeException(String.format("Unexpected execution context '%s' for session '%s' (expecting '%s')",
//                        executionContext, id, session.getExecutionContext()));
//            }
//            return session;
//        }
//
//        final var newSession = new SessionState(id, executionContext);
//        sessions.put(id, newSession);
//        return newSession;
//    }
//
//    private __ContextProvider getContextProvider(String sessionId) {
//        if (StringUtils.isBlank(sessionId)) throw new RuntimeException("Cannot get execution context for missing session ID");
//
//        final var session = getSession(sessionId);
//        final var executionContext = contextProviders.get(session.getExecutionContext());
//        if (executionContext == null) {
//            throw new RuntimeException(String.format("Cannot get execution context for session '%s'; unrecognized context type '%s'", sessionId, session.getExecutionContext()));
//        }
//
//        return executionContext;
//    }
//
//    private Mono<ModelResponse> submitPrompt(String sessionId, String prompt) {
//        final var session = getSession(sessionId);
//        final var body = getContextProvider(sessionId).buildBody(session, prompt);
//
//        return submitPrompt(sessionId, body);
//    }
//
//    private Mono<ModelResponse> submitPrompt(String sessionId, ModelRequest body) {
//        final var session = getSession(sessionId);
//
//        session.addEstimatedTokensForPrompt(body);
//
//        session.setLastPrompt(Optional.ofNullable(body.getContext())
//                .map(msgs -> msgs.get(msgs.size() - 1))
//                .map(ContextEntry::getContent)
//                .orElse(null));
//
//        // Insert temperature here based upon session state, so that it is applied whichever method we used to generate a new prompt
//        body.setTemperature(session.getCurrentTemperature());
//
//        return handlePreHooks(session, body)
//                .map(Mono::just)
//                .orElseGet(() -> client.submit(body));
//    }
//
//    private void recordPrompt(String sessionId, Prompt prompt) {
//        final var session = getSession(sessionId);
//
//        session.addEvent(ContextEntry.forUser(prompt.getPrompt()));
//        session.setCurrentTemperature(Optional.ofNullable(prompt.getTemperature()).orElse(0.7f));
//    }
//
//    private SessionState recordResponse(String sessionId, ModelResponse response) {
//        LOG.info("Response received: {}", Util.serializeOrThrow(response));
//
//        final var chosenResponse = Optional.ofNullable(response.getMessage())
//                .orElseThrow(() -> new RuntimeException("No response received"));
//
//        final var finalResponse = getContextProvider(sessionId).sanitizeResponse("```" + chosenResponse + "```");
//
//        final var session = getSession(sessionId);
//
//        session.setLastResponse(finalResponse);
//        session.getEvents().add(ContextEntry.forModel(finalResponse));
//
//        session.setIterationsRequired(1);
//
//        session.addTotalTokensUsed(response.getTotalTokenUsage());
//        session.addEstimatedTokensForResponse(response);
//
//        return session;
//    }
//
//    private Mono<SessionState> validateAndAttemptResponseCorrection(SessionState session) {
//        final var valid = validateResponse(session.getId(), session.getLastResponse());
//
//        if (!valid && PERFORM_AUTO_RESUBMISSION_ON_VALIDATION_FAILURE) {
//            final var contextProvider = getContextProvider(session.getId());
//            final var retryPrompt = contextProvider.getValidationFailureRetryPrompt(session.getLastResponse(), session.getValidationErrors());
//            final var body = contextProvider.buildUndecoratedBody(session, retryPrompt);
//
//            return submitPrompt(session.getId(), body)
//                    .map(resp -> recordResponse(session.getId(), resp))
//                    .map(s -> validateResponse(s.getId(), s.getLastResponse()))
//                    .map(nowValid -> {
//                        session.setIterationsRequired(session.getIterationsRequired() + 1);
//                        session.setValidOutput(nowValid);
//
//                        final var previousValidationErrors = Optional.ofNullable(session.getValidationErrors()).orElseGet(List::of)
//                                .stream().collect(Collectors.joining(",", "[", "]"));
//
//                        if (nowValid) {
//                            session.setValidationErrors(List.of(String.format("Output was regenerated due to validation failures and is now valid.  Previous failures: %s", previousValidationErrors)));
//                        }
//                        else {
//                            session.addValidationError(String.format("Attempted regeneration of output but could not resolve validation failures: %s", previousValidationErrors));
//                        }
//
//                        return session;
//                    });
//        }
//        else {
//            return Mono.just(session);
//        }
//    }
//
//    private boolean validateResponse(String sessionId, String response) {
//        final var session = getSession(sessionId);
//        final var validationErrors = getContextProvider(sessionId).validateResponse(response);
//
//        if (validationErrors.isEmpty()) {
//            session.setValidOutput(true);
//            session.setValidationErrors(List.of());
//        }
//        else {
//            session.setValidOutput(false);
//            session.setValidationErrors(validationErrors);
//        }
//
//        return session.getValidOutput();
//    }
//
//    private void updateBpmnContentIfValid(SessionState session) {
//        if (session == null) return;
//
//        if (session.hasValidationErrors() && !GENERATE_ON_VALIDATION_FAILURE) {
//            LOG.info("Not regenerating BPMN data for session '{}' since latest response has failed validation", session.getId());
//            return;
//        }
//
//        final var modelContent = getContextProvider(session.getId()).generateTransformedOutput(session.getLastResponse());
//        session.setTransformedContent(modelContent);
//    }
//
//    private Optional<ModelResponse> handlePreHooks(SessionState session, ModelRequest submission) {
//        if (session == null || submission == null) return Optional.empty();
//
//        final var lastPrompt = session.getLastPrompt();
//        if (lastPrompt == null) return Optional.empty();
//
//        // Handle <load:...> command
//        final var matcher = Constants.PATTERN_LOAD.matcher(lastPrompt);
//        if (matcher.find()) {
//            session.controlTokensResolved();
//            return Optional.ofNullable(new LlmMockClientImpl<>().loadMockResponse(matcher.group(1)));
//        }
//
//        return Optional.empty();
//    }
//
//    private String getKey() {
//        return Optional.ofNullable(environment)
//                .map(env -> env.getProperty("token"))
//                .map(k -> getClass().getClassLoader().getResource(k))
//                .map(url -> new File(url.getFile()))
//                .map(file -> {
//                    try {
//                        return Files.readString(file.toPath());
//                    }
//                    catch (Exception ex) {
//                        throw new RuntimeException("Failed to load token from file: " + ex.getMessage(), ex);
//                    }})
//                .orElseThrow(() -> new RuntimeException("Failed to load token"));
//    };
//}
