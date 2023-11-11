package org.rj.modelgen.llm.model;

import org.rj.modelgen.llm.beans.ExecutionContext;
import org.rj.modelgen.llm.client.LlmClient;
import org.rj.modelgen.llm.exception.LlmGenerationConfigException;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.session.SessionState;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ModelInterface {
    private final LlmClient client;
    private final Function<String, SessionState> sessionGenerator;
    private final ConcurrentMap<String, SessionState> sessions;

    public ModelInterface(LlmClient client) {
        this(client, SessionState::new);
    }
    public ModelInterface(LlmClient client, Function<String, SessionState> sessionGenerator) {
        this.client = client;
        this.sessionGenerator = sessionGenerator;
        this.sessions = new ConcurrentHashMap<>();
    }

    protected Mono<Optional<SessionState>> onNewSessionCreated(SessionState sessionState) {
        return Mono.just(Optional.empty());
    }

    public final Mono<SessionState> createSession(String id) {
        return Mono.just(getOrCreateSession(id))

                // Allow update of the newly-created session via `onNewSessionCreated` event if implemented by subclasses
                .flatMap(this::onNewSessionCreated)
                .map(newSessionData -> newSessionData
                        .map(this::updateSession)
                        .orElseGet(() -> getSession(id).orElseThrow(() -> new LlmGenerationConfigException("Failed to retrieve new session"))));
    }

    public final Mono<SessionState> createSessionIfRequired(String id) {
        return getSession(id)
                .map(Mono::just)
                .orElseGet(() -> createSession(id));
    }

    protected void onSubmissionStart(String id, ModelRequest request, ModelRequestHttpOptions httpOptions) { }
    protected Mono<ModelResponse> onSubmissionComplete(String id, ModelRequest request, ModelRequestHttpOptions httpOptions, ModelResponse response) {
        return Mono.just(response);
    }

    public final Mono<ModelResponse> submit(String id, ModelRequest request) {
        return submit(id, request, null);
    }
    public final Mono<ModelResponse> submit(String id, ModelRequest request, ModelRequestHttpOptions httpOptions) {
        onSubmissionStart(id, request, httpOptions);
        getOrCreateSession(id).recordUserPrompt(request);

        return client.submit(request, httpOptions)
                .doOnNext(resp -> recordResponse(id, resp))
                .doOnNext(resp -> onSubmissionComplete(id, request, httpOptions, resp));
    }

    private void recordResponse(String id, ModelResponse response) {
        getOrCreateSession(id).recordModelResponse(response);
    }

    public final Optional<SessionState> getSession(String id) {
        return Optional.ofNullable(id).map(x -> sessions.getOrDefault(x, null));
    }

    public final boolean sessionExists(String id) {
        return sessions.containsKey(id);
    }

    // TODO: Replace with getSession() if session creation is now explicit
    public final SessionState getOrCreateSession(String id) {
        if (id == null) throw new LlmGenerationModelException("Invalid null session ID");
        return sessions.computeIfAbsent(id, sessionGenerator);
    }

    public final SessionState updateSession(SessionState sessionState) {
        if (sessionState == null) throw new LlmGenerationModelException("Invalid null session state");
        sessions.put(sessionState.getId(), sessionState);

        return sessionState;
    }

}
