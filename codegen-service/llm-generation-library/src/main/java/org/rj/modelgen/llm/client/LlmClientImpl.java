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

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + getKey(environment))
                .build();
    }

    public Mono<ModelResponse> submit(ModelRequest request, ModelRequestHttpOptions httpOptions) {
        final var reqId = requestId.getAndIncrement();
        LOG.info("LLM client submitting request {}: {}", reqId, Util.serializeOrThrow(request));

        final TModelRequest submissionPayload = config.getRequestTransformer().transform(request);
        final var submissionPayloadBytes = Util.serializeBinaryOrThrow(submissionPayload, ex -> new RuntimeException(
                String.format("Failed to serialize model request to submission payload (%s)", ex.getMessage()), ex));

        return client.request(config.getSubmissionMethod())
                .uri(config.getSubmissionUri())
                .send((clientRequest, outbound) -> {
                    clientRequest = decorateClientRequest(clientRequest, httpOptions);
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

    private HttpClientRequest decorateClientRequest(HttpClientRequest clientRequest, ModelRequestHttpOptions httpOptions) {
        if (clientRequest == null || httpOptions == null) return clientRequest;

        // Append headers
        Optional.ofNullable(config.getDefaultHeaders()).orElseGet(Map::of).forEach(clientRequest::addHeader);  // Default
        Optional.ofNullable(httpOptions.getHeaders()).orElseGet(Map::of).forEach(clientRequest::addHeader);    // Per-request

        clientRequest.responseTimeout(Duration.ofSeconds(config.getResponseTimeout()));

        return clientRequest;
    }

    private String getKey(Environment environment) {
        return Optional.ofNullable(environment)
                .map(env -> env.getProperty("token"))
                .map(k -> getClass().getClassLoader().getResource(k))
                .map(url -> new File(url.getFile()))
                .map(file -> {
                    try {
                        return Files.readString(file.toPath());
                    }
                    catch (Exception ex) {
                        throw new RuntimeException("Failed to load token from file: " + ex.getMessage(), ex);
                    }})
                .orElseThrow(() -> new RuntimeException("Failed to load token"));
    };
}
