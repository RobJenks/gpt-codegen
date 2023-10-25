package org.rj.modelgen.llm.client;

import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.resources.ConnectionProvider;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class LlmClientImpl<TModelRequest, TModelResponse> implements LlmClient {
    private static final Logger LOG = LoggerFactory.getLogger(LlmClientImpl.class);
    private final LlmClientConfig<TModelRequest, TModelResponse> config;
    private final HttpClient client;
    private final AtomicInteger requestId = new AtomicInteger(0);

    public LlmClientImpl(LlmClientConfig<TModelRequest, TModelResponse> config) {
        this.config = config;
        this.client = buildClient(config);
    }

    private HttpClient buildClient(LlmClientConfig<TModelRequest, TModelResponse> config) {
        final var httpClient = HttpClient.create(ConnectionProvider.builder("custom")
                        .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTime()))
                        .build())
                .baseUrl(config.getBaseUrl())
                .responseTimeout(Duration.ofSeconds(config.getResponseTimeout()));

        return httpClient;
    }

    public Mono<ModelResponse> submit(ModelRequest request, ModelRequestHttpOptions httpOptions) {
        final var reqId = requestId.getAndIncrement();
        LOG.info("LLM client received submission request {}: {}", reqId, Util.serializeOrThrow(request));

        final TModelRequest submissionPayload = config.getRequestTransformer().transform(request);
        LOG.info("LLM client submitting transformed request {} to target: {}", reqId, Util.serializeOrThrow(submissionPayload));
        final var submissionPayloadBytes = Util.serializeBinaryOrThrow(submissionPayload, ex -> new RuntimeException(
                String.format("Failed to serialize model request to submission payload (%s)", ex.getMessage()), ex));

        return client.request(config.getSubmissionMethod())
                .uri(absoluteUri(config.getSubmissionUri()))
                .send((clientRequest, outbound) -> {
                    clientRequest = decorateBaseClientRequest(clientRequest, httpOptions);
                    clientRequest = config.decorateClientRequest(clientRequest, httpOptions);
                    return outbound.sendByteArray(Mono.just(submissionPayloadBytes));
                })
                .responseSingle((response, body) -> body.asByteArray()
                                .map(serialized -> Util.deserializeBinaryOrThrow(serialized, config.getResponseClass()))
                                .map(config.getResponseTransformer()::transform)
                )
                .doOnError(t -> LOG.error("LLM client received submission error for request {}: {}", reqId, t.getMessage(), t))
                .doOnSuccess(res -> LOG.info("LLM client received response for request {}: {}", reqId, Util.serializeOrThrow(res)))
                .timeout(Duration.ofSeconds(240L));
    }

    private HttpClientRequest decorateBaseClientRequest(HttpClientRequest clientRequest, ModelRequestHttpOptions httpOptions) {
        if (clientRequest == null) return null;

        // Append default headers
        Optional.ofNullable(config.getDefaultHeaders()).orElseGet(Map::of).forEach(clientRequest::addHeader);

        // Append per-request headers, if applicable
        if (httpOptions != null) {
            Optional.ofNullable(httpOptions.getHeaders()).orElseGet(Map::of).forEach(clientRequest::addHeader);
        }

        clientRequest.responseTimeout(Duration.ofSeconds(config.getResponseTimeout()));

        return clientRequest;
    }

    private URI absoluteUri(URI relative) {
        return URI.create(config.getBaseUrl()).resolve(relative);
    }
}
