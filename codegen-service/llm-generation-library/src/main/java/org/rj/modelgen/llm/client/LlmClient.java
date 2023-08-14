package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import reactor.core.publisher.Mono;

public interface LlmClient {

    Mono<ModelResponse> submit(ModelRequest request, ModelRequestHttpOptions httpOptions);

}
