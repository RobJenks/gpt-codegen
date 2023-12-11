package org.rj.modelgen.llm.client;

import io.netty.handler.codec.http.HttpMethod;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.request.ModelRequestTransformer;
import org.rj.modelgen.llm.response.ModelResponseTransformer;
import reactor.netty.http.client.HttpClientRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class LlmClientConfig<TModelRequest, TModelResponse> {
    private LlmClientType type;

    private Class<TModelRequest> requestClass;
    private Class<TModelResponse> responseClass;

    private ModelRequestTransformer<TModelRequest> requestTransformer;
    private ModelResponseTransformer<TModelResponse> responseTransformer;

    public LlmClientConfig(Class<TModelRequest> requestClass, Class<TModelResponse> responseClass) {
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    public Class<TModelRequest> getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class<TModelRequest> requestClass) {
        this.requestClass = requestClass;
    }

    public Class<TModelResponse> getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class<TModelResponse> responseClass) {
        this.responseClass = responseClass;
    }

    public LlmClientType getType() {
        return type;
    }

    public void setType(LlmClientType type) {
        this.type = type;
    }

    public ModelRequestTransformer<TModelRequest> getRequestTransformer() {
        return requestTransformer;
    }

    public void setRequestTransformer(ModelRequestTransformer<TModelRequest> requestTransformer) {
        this.requestTransformer = requestTransformer;
    }

    public ModelResponseTransformer<TModelResponse> getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(ModelResponseTransformer<TModelResponse> responseTransformer) {
        this.responseTransformer = responseTransformer;
    }


}
