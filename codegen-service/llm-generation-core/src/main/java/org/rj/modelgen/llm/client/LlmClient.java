package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.beans.SubmissionMetadata;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.session.SessionState;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface LlmClient {

    Mono<ModelResponse> submitModelRequest(ModelRequest request, Map<String, Object> sessionMetadata, ModelRequestHttpOptions httpOptions);

}
