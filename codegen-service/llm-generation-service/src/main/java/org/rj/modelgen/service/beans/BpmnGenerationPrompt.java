package org.rj.modelgen.service.beans;

public class BpmnGenerationPrompt {
    private String prompt;
    private Double temperature;

    public BpmnGenerationPrompt() { }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
