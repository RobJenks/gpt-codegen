package org.rj.codegen.codegenservice.gpt.client;

import org.rj.codegen.codegenservice.gpt.beans.PromptContextSubmission;
import org.rj.codegen.codegenservice.gpt.beans.SubmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;

public class GptClientImpl implements GptClient {
    private static final Logger LOG = LoggerFactory.getLogger(GptClientImpl.class);
    private final WebClient client;

    public GptClientImpl(Environment environment) {
        client = buildClient(environment);
    }

    private WebClient buildClient(Environment environment) {
        final var httpClient = HttpClient.create(ConnectionProvider.builder("custom")
                        .maxIdleTime(Duration.ofSeconds(120L)).build());
        httpClient.responseTimeout(Duration.ofSeconds(300L));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + getKey(environment))
                .build();
    }

    public Mono<SubmissionResponse> submit(PromptContextSubmission submission) {
        return client.post()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .body(BodyInserters.fromValue(submission))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(SubmissionResponse.class)
                .doOnError(t -> LOG.error("ERROR OCCURRED: " + t))
                .map(ResponseEntity::getBody)
                .timeout(Duration.ofSeconds(240L));
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
