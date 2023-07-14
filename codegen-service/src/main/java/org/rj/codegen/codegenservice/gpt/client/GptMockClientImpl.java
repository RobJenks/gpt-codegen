package org.rj.codegen.codegenservice.gpt.client;

import org.rj.codegen.codegenservice.gpt.beans.PromptContextSubmission;
import org.rj.codegen.codegenservice.gpt.beans.SubmissionResponse;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

public class GptMockClientImpl implements GptClient {
    private static final Logger LOG = LoggerFactory.getLogger(GptMockClientImpl.class);
    private final SubmissionResponse mockResponse;
    public GptMockClientImpl(Environment environment) {
        mockResponse = loadMockResponse(environment);
    }

    private SubmissionResponse loadMockResponse(Environment environment) {
        final var mockSource = environment.getProperty("gptclient.mock.source", "content/samples/gpt-client-mock-response.json");
        return Util.loadOptionalStringResource(mockSource)
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
