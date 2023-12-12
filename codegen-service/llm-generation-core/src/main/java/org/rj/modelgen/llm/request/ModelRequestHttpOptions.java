package org.rj.modelgen.llm.request;

import java.util.*;

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

    public Optional<List<String>> getHeader(String key) {
        return Optional.ofNullable(getHeaders())
                .map(headers -> headers.get(key));
    }

    public Optional<String> getFirstHeader(String key) {
        return getHeader(key)
                .filter(values-> !values.isEmpty())
                .map(values -> values.get(0));
    }

    public Optional<String> getCookie(String key) {
        return Optional.ofNullable(getHeaders())
                .map(headers -> headers.get("Cookie"))
                .flatMap(cookies -> cookies.stream()
                        .filter(c -> c != null && c.startsWith(key + "="))
                        .findFirst());
    }

}
