package org.rj.modelgen.service.beans;

public class BpmnGenerationPrompt {
    private String prompt;
    private Double temperature;

    public BpmnGenerationPrompt() { }

    public BpmnGenerationPrompt(String prompt, double temperature) {
        this.prompt = prompt;
        this.temperature = temperature;
    }

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
