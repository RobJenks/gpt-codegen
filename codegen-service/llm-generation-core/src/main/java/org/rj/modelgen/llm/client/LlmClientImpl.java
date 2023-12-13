package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.beans.SubmissionMetadata;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LlmClientImpl<TModelRequest, TModelResponse> implements LlmClient {
    private static final Logger LOG = LoggerFactory.getLogger(LlmClientImpl.class);
    private static final boolean LOG_RAW_RESPONSE_DATA = true;
    private final LlmClientConfig<TModelRequest, TModelResponse> config;
    private final AtomicInteger requestId = new AtomicInteger(0);

    public LlmClientImpl(LlmClientConfig<TModelRequest, TModelResponse> config) {
        this.config = config;
    }

    public final Mono<ModelResponse> submitModelRequest(ModelRequest request, Map<String, Object> sessionMetadata, ModelRequestHttpOptions httpOptions) {
        final var reqId = requestId.getAndIncrement();
        LOG.info("LLM client received submission request {}: {}", reqId, Util.serializeOrThrow(request));

        final var metadata = new SubmissionMetadata(reqId, sessionMetadata, httpOptions);

        return Mono.just(request)
                .map(config.getRequestTransformer()::transform)
                .flatMap(req -> evaluateModel(req, metadata))
                .doOnNext(resp -> logRawResponseData(reqId, resp))
                .map(config.getResponseTransformer()::transform);
    }

    protected abstract Mono<TModelResponse> evaluateModel(TModelRequest request, SubmissionMetadata submissionMetadata);

    protected void logRawResponseData(int requestId, TModelResponse response) {
        if (!LOG_RAW_RESPONSE_DATA) return;

        try {
            final var str = Util.getObjectMapper().writeValueAsString(response);
            LOG.info("Received raw response data for request {}: {}", requestId, str);
        }
        catch (Exception ex) {
            LOG.error("Error while trying to deserialize raw response data for request {}", requestId, ex);
        }
    }

    protected LlmClientConfig<TModelRequest, TModelResponse> getConfig() {
        return config;
    }

}
