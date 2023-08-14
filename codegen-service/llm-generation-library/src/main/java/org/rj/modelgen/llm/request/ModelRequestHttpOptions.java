package org.rj.modelgen.llm.request;

import java.util.HashMap;
import java.util.Map;

public class ModelRequestHttpOptions {
    private Map<String, String> headers = new HashMap<>();

    public ModelRequestHttpOptions() { }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
