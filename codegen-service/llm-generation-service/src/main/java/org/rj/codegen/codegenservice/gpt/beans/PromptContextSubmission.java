package org.rj.codegen.codegenservice.gpt.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.codegen.codegenservice.util.Constants;
import org.rj.codegen.codegenservice.util.Util;

import java.util.List;
import java.util.Optional;

public class PromptContextSubmission {
    private String model;
    private float temperature;
    private List<ContextEntry> messages;

    public static PromptContextSubmission defaultConfig(List<ContextEntry> context) {
        //return new PromptContextSubmission("gpt-3.5-turbo", 0.7f, context);
        return new PromptContextSubmission("gpt-4", 0.7f, context);
    }

    public PromptContextSubmission() { }

    public PromptContextSubmission(String model, float temperature, List<ContextEntry> messages) {
        this.model = model;
        this.temperature = temperature;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public List<ContextEntry> getMessages() {
        return messages;
    }

    public void setMessages(List<ContextEntry> messages) {
        this.messages = messages;
    }

    @JsonIgnore
    public int estimateTokenSize(boolean includeAssistantEvents) {
        return Optional.ofNullable(messages).orElseGet(List::of).stream()
                .filter(entry -> includeAssistantEvents || Constants.ROLE_USER.equals(entry.getRole()))
                .map(ContextEntry::getContent)
                .map(x -> Util.estimateTokenSize(x) + 1)    // + 1 for `role`
                .reduce(Integer::sum)
                .orElse(0);
    }
}
