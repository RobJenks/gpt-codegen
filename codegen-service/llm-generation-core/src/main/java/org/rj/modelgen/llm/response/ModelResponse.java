package org.rj.modelgen.llm.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public class ModelResponse {
    public enum Status {
        SUCCESS,
        FAILED
    }

    private Status status;
    private String message;
    private String error;
    private int promptTokenUsage;
    private int responseTokenUsage;
    private Map<String, Object> metadata;

    public ModelResponse() { }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return Status.SUCCESS.equals(status);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getPromptTokenUsage() {
        return promptTokenUsage;
    }

    public void setPromptTokenUsage(int promptTokenUsage) {
        this.promptTokenUsage = promptTokenUsage;
    }

    public int getResponseTokenUsage() {
        return responseTokenUsage;
    }

    public void setResponseTokenUsage(int responseTokenUsage) {
        this.responseTokenUsage = responseTokenUsage;
    }

    @JsonIgnore
    public int getTotalTokenUsage() {
        return promptTokenUsage + responseTokenUsage;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
