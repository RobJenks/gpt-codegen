package org.rj.modelgen.llm.integrations.openai;

import io.netty.handler.codec.http.HttpMethod;
import org.rj.modelgen.llm.beans.SubmissionMetadata;
import org.rj.modelgen.llm.client.LlmClientImpl;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class OpenAIClient extends LlmClientImpl<OpenAIModelRequest, OpenAIModelResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenAIClient.class);

    private final OpenAIClientConfig config;
    private final HttpClient client;

    public OpenAIClient(OpenAIClientConfig config) {
        super(config);
        this.config = config;
        this.client = buildClient(config);
    }

    private HttpClient buildClient(OpenAIClientConfig config) {
        return HttpClient.create(ConnectionProvider.builder("custom")
                        .maxIdleTime(Duration.ofSeconds(config.getMaxIdleTime()))
                        .build())
                .baseUrl(config.getBaseUrl())
                .responseTimeout(Duration.ofSeconds(config.getResponseTimeout()));
    }

    @Override
    protected Mono<OpenAIModelResponse> evaluateModel(OpenAIModelRequest openAIModelRequest, SubmissionMetadata submissionMetadata) {
        final var submissionPayloadBytes = Util.serializeBinaryOrThrow(openAIModelRequest, ex -> new RuntimeException(
                String.format("Failed to serialize model request to submission payload (%s)", ex.getMessage()), ex));

        return client.wiretap(true).request(HttpMethod.POST)
                .uri(absoluteUri(getSubmissionUri()))
                .send((clientRequest, outbound) -> {
                    clientRequest = decorateClientRequest(clientRequest, submissionMetadata.getHttpOptions());
                    return outbound.sendByteArray(Mono.just(submissionPayloadBytes));
                })
                .responseSingle((response, body) -> body.asByteArray()
                        .map(serialized -> Util.deserializeBinaryOrThrow(serialized, config.getResponseClass()))
                )
                .doOnError(t -> LOG.error("LLM client received submission error for request {}: {}", submissionMetadata.getRequestId(), t.getMessage(), t))
                .doOnSuccess(res -> LOG.info("LLM client received response for request {}: {}", submissionMetadata.getRequestId(), Util.serializeOrThrow(res)))
                .timeout(Duration.ofSeconds(240L));
    }

    protected HttpClientRequest decorateClientRequest(HttpClientRequest clientRequest, ModelRequestHttpOptions httpOptions) {
        if (clientRequest == null) return null;

        // Append default headers
        Optional.ofNullable(config.getDefaultHeaders()).orElseGet(Map::of).forEach(clientRequest::addHeader);

        // Append per-request headers, if applicable
        if (httpOptions != null) {
            Optional.ofNullable(httpOptions.getHeaders()).orElseGet(Map::of).forEach(
                    (key, values) -> values.forEach(value -> clientRequest.addHeader(key, value)));
        }

        // Apply config to the request
        config.decorateClientRequest(clientRequest, httpOptions);
        clientRequest.responseTimeout(Duration.ofSeconds(config.getResponseTimeout()));

        return clientRequest;
    }

    private URI getSubmissionUri() {
        return URI.create("v1/chat/completions");
    }

    private URI absoluteUri(URI relative) {
        return URI.create(config.getBaseUrl()).resolve(relative);
    }
}
