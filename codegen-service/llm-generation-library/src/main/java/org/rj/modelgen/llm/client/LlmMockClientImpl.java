package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class LlmMockClientImpl<TModelRequest, TModelResponse> implements LlmClient {
    private static final Logger LOG = LoggerFactory.getLogger(LlmMockClientImpl.class);
    private final ModelResponse mockResponse;

    public LlmMockClientImpl() {
        this(null);
    }
    public LlmMockClientImpl(String mockSource) {
        mockResponse = loadMockResponse(mockSource);
    }


    public ModelResponse loadMockResponse(String mockSource) {
        return Util.loadOptionalStringResource(mockSource)
                .map(serialized -> Util.deserializeOrThrow(serialized, ModelResponse.class,
                        ex -> new RuntimeException("Could not deserialize mock LLM client response: " + ex.getMessage(), ex)))
                .orElseGet(() -> {
                    LOG.warn("No mock response data provided for LLM mock client");
                    return null;
                });
    }

    @Override
    public Mono<ModelResponse> submit(ModelRequest modelRequest, ModelRequestHttpOptions httpOptions) {
        return Mono.just(mockResponse);
    }
}
