package org.rj.modelgen.llm.integrations.openai;

import io.netty.handler.codec.http.HttpMethod;
import org.rj.modelgen.llm.client.LlmClientConfig;
import org.rj.modelgen.llm.client.LlmClientType;

import java.net.URI;
import java.util.Map;

public class OpenAIClientConfig extends LlmClientConfig<OpenAIModelRequest, OpenAIModelResponse> {
    public OpenAIClientConfig() {
        super(OpenAIModelRequest.class, OpenAIModelResponse.class);

        setType(LlmClientType.Default);
        setBaseUrl("https://api.openai.com");
        setSubmissionUri(URI.create("/v1/chat/completions"));
        setSubmissionMethod(HttpMethod.POST);

        setDefaultHeaders(Map.of(
                "Accept", "application/json",
                "Content-Type", "application/json"));

        setRequestTransformer(new OpenAIModelRequestTransformer());
        setResponseTransformer(new OpenAIModelResponseTransformer());
    }
}
