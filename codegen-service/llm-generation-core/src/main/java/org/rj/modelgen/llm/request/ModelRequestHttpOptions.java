package org.rj.modelgen.llm.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelRequestHttpOptions {
    private Map<String, List<String>> headers = new HashMap<>();

    public ModelRequestHttpOptions() { }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        if (key == null) throw new IllegalArgumentException("Cannot add header with null key");

        final var values = headers.computeIfAbsent(key, __ -> new ArrayList<>());
        values.add(value);
    }

    public void addCookie(String key, String value) {
        if (key == null || value == null) throw new IllegalArgumentException("Cannot add cookie with null key or value");

        addHeader("Cookie", String.format("%s=%s", key, value));
    }

}
