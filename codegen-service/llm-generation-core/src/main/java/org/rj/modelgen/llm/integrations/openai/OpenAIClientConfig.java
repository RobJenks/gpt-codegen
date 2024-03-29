package org.rj.modelgen.llm.integrations.openai;

import org.rj.modelgen.llm.client.LlmClientConfig;
import org.rj.modelgen.llm.client.LlmClientType;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;
import java.util.function.Supplier;

public class OpenAIClientConfig extends LlmClientConfig<OpenAIModelRequest, OpenAIModelResponse> {
    private Supplier<String> apiKeyGenerator;

    public OpenAIClientConfig(Supplier<String> apiKeyGenerator) {
        super(OpenAIModelRequest.class, OpenAIModelResponse.class);

        setType(LlmClientType.Default);

        setRequestTransformer(new OpenAIModelRequestTransformer());
        setResponseTransformer(new OpenAIModelResponseTransformer());

        if (apiKeyGenerator == null) {
            throw new IllegalArgumentException("Cannot initialize OpenAI LLM client without valid API key generator");
        }
        this.apiKeyGenerator = apiKeyGenerator;
    }

    public HttpClientRequest decorateClientRequest(HttpClientRequest clientRequest, ModelRequestHttpOptions httpOptions) {
        return clientRequest.addHeader("Authorization", "Bearer " + apiKeyGenerator.get());
    }

    public String getBaseUrl() {
        return "https://api.openai.com/";
    }

    public Map<String, String> getDefaultHeaders() {
        return Map.of(
                "Accept", "application/json",
                "Content-Type", "application/json");
    }

    public long getResponseTimeout() {
        return 300L;
    }

    public long getMaxIdleTime() {
        return 120L;
    }

}
