package org.rj.modelgen.llm.client;

import io.netty.handler.codec.http.HttpMethod;
import org.rj.modelgen.llm.request.ModelRequestTransformer;
import org.rj.modelgen.llm.response.ModelResponseTransformer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LlmClientConfig<TModelRequest, TModelResponse> {
    private static final long DEFAULT_RESPONSE_TIMEOUT = 300L;
    private static final long DEFAULT_MAX_IDLE_TIME = 120L;

    private Class<TModelRequest> requestClass;
    private Class<TModelResponse> responseClass;
    private LlmClientType type;
    private String baseUrl;
    private URI submissionUri;
    private HttpMethod submissionMethod;
    private Map<String, String> defaultHeaders = new HashMap<>();
    private ModelRequestTransformer<TModelRequest> requestTransformer;
    private ModelResponseTransformer<TModelResponse> responseTransformer;
    private long responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
    private long maxIdleTime = DEFAULT_MAX_IDLE_TIME;

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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URI getSubmissionUri() {
        return submissionUri;
    }

    public void setSubmissionUri(URI submissionUri) {
        this.submissionUri = submissionUri;
    }

    public HttpMethod getSubmissionMethod() {
        return submissionMethod;
    }

    public void setSubmissionMethod(HttpMethod submissionMethod) {
        this.submissionMethod = submissionMethod;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
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

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
