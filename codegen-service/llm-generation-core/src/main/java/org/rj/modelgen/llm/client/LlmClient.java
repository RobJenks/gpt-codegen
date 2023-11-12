package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.session.SessionState;
import reactor.core.publisher.Mono;

public interface LlmClient {

    default Mono<ModelResponse> submit(ModelRequest request, SessionState session) {
        return submit(request, session, null);
    }

    Mono<ModelResponse> submit(ModelRequest request, SessionState session, ModelRequestHttpOptions httpOptions);

}
