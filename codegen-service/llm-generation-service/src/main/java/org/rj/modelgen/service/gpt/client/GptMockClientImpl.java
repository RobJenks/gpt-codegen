package org.rj.modelgen.service.gpt.client;

import org.rj.modelgen.service.gpt.beans.PromptContextSubmission;
import org.rj.modelgen.service.gpt.beans.SubmissionResponse;
import org.rj.modelgen.service.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

public class GptMockClientImpl implements GptClient {
    private static final Logger LOG = LoggerFactory.getLogger(GptMockClientImpl.class);
    private final SubmissionResponse mockResponse;

    public GptMockClientImpl() {
        this(null);
    }
    public GptMockClientImpl(Environment environment) {
        mockResponse = loadMockResponse(environment);
    }

    private SubmissionResponse loadMockResponse(Environment environment) {
        if (environment == null) return null;

        final var mockSource = environment.getProperty("gptclient.mock.source", "content/samples/gpt-client-mock-response.json");
        return loadMockResponse(mockSource);
    }

    public SubmissionResponse loadMockResponse(String source) {
        return Util.loadOptionalStringResource(source)
                .map(serialized -> Util.deserializeOrThrow(serialized, SubmissionResponse.class,
                        ex -> new RuntimeException("Could not deserialize mock GPT client response: " + ex.getMessage(), ex)))
                .orElseGet(() -> {
                    LOG.warn("No mock response data provided for GptMockClient");
                    return null;
                });
    }

    @Override

    public Mono<SubmissionResponse> submit(PromptContextSubmission submission) {
        return Mono.just(mockResponse);
    }
}
