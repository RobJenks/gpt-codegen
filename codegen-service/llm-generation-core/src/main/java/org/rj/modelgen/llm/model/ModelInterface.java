package org.rj.modelgen.llm.model;

import org.rj.modelgen.llm.client.LlmClient;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.session.SessionState;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ModelInterface {
    private final LlmClient client;
    private final ContextProvider contextProvider;
    private final ConcurrentMap<String, SessionState> sessions;

    public ModelInterface(LlmClient client, ContextProvider contextProvider) {
        this.client = client;
        this.contextProvider = contextProvider;
        this.sessions = new ConcurrentHashMap<>();
    }

    public Optional<SessionState> getSessionState(String id) {
        return Optional.ofNullable(id).map(x -> sessions.getOrDefault(x, null));
    }
}
