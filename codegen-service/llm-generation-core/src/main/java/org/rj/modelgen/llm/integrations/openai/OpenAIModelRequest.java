package org.rj.modelgen.llm.integrations.openai;

import java.util.List;

public class OpenAIModelRequest {
    private String model;
    private float temperature;
    private List<OpenAIContextMessage> messages;

    public static OpenAIModelRequest defaultConfig(List<OpenAIContextMessage> context) {
        return new OpenAIModelRequest("gpt-4", 0.7f, context);
    }

    public OpenAIModelRequest() { }

    public OpenAIModelRequest(String model, float temperature, List<OpenAIContextMessage> messages) {
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

    public List<OpenAIContextMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<OpenAIContextMessage> messages) {
        this.messages = messages;
    }

}
