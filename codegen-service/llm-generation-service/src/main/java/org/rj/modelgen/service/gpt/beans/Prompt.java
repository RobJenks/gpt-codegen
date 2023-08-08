package org.rj.modelgen.service.gpt.beans;

public class Prompt {
    private String prompt;
    private Float temperature;

    public Prompt() { }

    public Prompt(String prompt, float temperature) {
        this.prompt = prompt;
        this.temperature = temperature;
    }

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
