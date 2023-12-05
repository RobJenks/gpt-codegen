package org.rj.modelgen.llm.beans;

import org.rj.modelgen.llm.request.ModelRequestHttpOptions;

import java.util.Map;
import java.util.Optional;

public class SubmissionMetadata {
    private int requestId;
    private Map<String, Object> sessionMetadata;
    private ModelRequestHttpOptions httpOptions;

    public SubmissionMetadata(int requestId, Map<String, Object> sessionMetadata, ModelRequestHttpOptions httpOptions) {
        setRequestId(requestId);
        setSessionMetadata(sessionMetadata);
        setHttpOptions(httpOptions);
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Map<String, Object> getSessionMetadata() {
        return sessionMetadata;
    }

    public void setSessionMetadata(Map<String, Object> sessionMetadata) {
        this.sessionMetadata = Optional.ofNullable(sessionMetadata).orElseGet(Map::of);
    }

    public ModelRequestHttpOptions getHttpOptions() {
        return httpOptions;
    }

    public void setHttpOptions(ModelRequestHttpOptions httpOptions) {
        this.httpOptions = httpOptions;
    }
}
