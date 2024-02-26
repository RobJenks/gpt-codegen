package org.rj.modelgen.llm.beans;

public class Prompt {
    private String prompt;
    private Double temperature;

    public Prompt() { }

    public Prompt(String prompt, Double temperature) {
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
