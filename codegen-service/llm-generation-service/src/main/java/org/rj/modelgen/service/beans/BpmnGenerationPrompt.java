package org.rj.modelgen.service.beans;

public class BpmnGenerationPrompt {
    private String prompt;
    private Float temperature;

    public BpmnGenerationPrompt() { }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
}
