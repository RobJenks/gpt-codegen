package org.rj.modelgen.llm.integrations.openai;

import io.netty.handler.codec.http.HttpMethod;
import org.rj.modelgen.llm.client.LlmClientConfig;
import org.rj.modelgen.llm.client.LlmClientType;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import reactor.netty.http.client.HttpClientRequest;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

public class OpenAIClientConfig extends LlmClientConfig<OpenAIModelRequest, OpenAIModelResponse> {
    private Supplier<String> apiKeyGenerator;

    public OpenAIClientConfig(Supplier<String> apiKeyGenerator) {
        super(OpenAIModelRequest.class, OpenAIModelResponse.class);

        setType(LlmClientType.Default);
        setBaseUrl("https://api.openai.com/");
        setSubmissionUri(URI.create("v1/chat/completions"));
        setSubmissionMethod(HttpMethod.POST);

        setDefaultHeaders(Map.of(
                "Accept", "application/json",
                "Content-Type", "application/json"));

        setRequestTransformer(new OpenAIModelRequestTransformer());
        setResponseTransformer(new OpenAIModelResponseTransformer());

        if (apiKeyGenerator == null) {
            throw new IllegalArgumentException("Cannot initialize OpenAI LLM client without valid API key generator");
        }
        this.apiKeyGenerator = apiKeyGenerator;
    }

    @Override
    public HttpClientRequest decorateClientRequest(HttpClientRequest clientRequest, ModelRequestHttpOptions httpOptions) {
        return clientRequest.addHeader("Authorization", "Bearer " + apiKeyGenerator.get());
    }
}
