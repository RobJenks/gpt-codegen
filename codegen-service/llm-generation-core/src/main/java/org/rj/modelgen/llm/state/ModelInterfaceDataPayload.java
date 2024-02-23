package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.util.Util;

import java.util.Map;

public class ModelInterfaceDataPayload {
    private String sessionId;
    private String request;
    private String llm;
    private float temperature;

    public ModelInterfaceDataPayload(String sessionId, String request) {
        this.sessionId = sessionId;
        this.request = request;

        // Default values for optional parameters
        this.llm = "gpt-4";
        this.temperature = 0.7f;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getLlm() {
        return llm;
    }

    public void setLlm(String llm) {
        this.llm = llm;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }


    public Map<String, Object> toJsonPayload() {
        final var serialized = Util.serializeOrThrow(this, ex -> new RuntimeException(
                String.format("Could not serialize model input payload (%s)", ex.getMessage()), ex));

        final var payload = Util.deserializeOrThrow(serialized, Map.class, ex -> new RuntimeException(
                String.format("Could not deserialize model input payload to JSON map (%s)", ex.getMessage()), ex));

        return payload;
    }
}
