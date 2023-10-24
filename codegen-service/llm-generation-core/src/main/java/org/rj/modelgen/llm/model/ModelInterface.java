package org.rj.modelgen.llm.model;

import org.rj.modelgen.llm.beans.ExecutionContext;
import org.rj.modelgen.llm.client.LlmClient;
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

public class ModelInterface {
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

    public Mono<ModelResponse> submit(String id, ModelRequest request) {
        return submit(id, request, null);
    }
    public Mono<ModelResponse> submit(String id, ModelRequest request, ModelRequestHttpOptions httpOptions) {
        getOrCreateSession(id).recordUserPrompt(request);

        return client.submit(request, httpOptions)
                .doOnNext(resp -> recordResponse(id, resp));
    }

    private void recordResponse(String id, ModelResponse response) {
        getOrCreateSession(id).recordModelResponse(response);
    }

    public Optional<SessionState> getSession(String id) {
        return Optional.ofNullable(id).map(x -> sessions.getOrDefault(x, null));
    }

    public SessionState getOrCreateSession(String id) {
        if (id == null) throw new LlmGenerationModelException("Invalid null session ID");
        return sessions.computeIfAbsent(id, sessionGenerator);
    }

}
