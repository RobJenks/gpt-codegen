package org.rj.modelgen.llm.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.ContextRole;
import org.rj.modelgen.llm.util.Util;

import java.util.List;
import java.util.Optional;

public class ModelRequest {
    private String model;
    private float temperature;
    private List<ContextEntry> context;


    public ModelRequest() { }

    public ModelRequest(String model, float temperature, List<ContextEntry> context) {
        this.model = model;
        this.temperature = temperature;
        this.context = context;
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

    public List<ContextEntry> getContext() {
        return context;
    }

    public void setContext(List<ContextEntry> context) {
        this.context = context;
    }

    @JsonIgnore
    public int estimateTokenSize(boolean includeAssistantEvents) {
        return Optional.ofNullable(context).orElseGet(List::of).stream()
                .filter(entry -> (includeAssistantEvents || entry.getRole() == ContextRole.USER))
                .map(ContextEntry::getContent)
                .map(x -> Util.estimateTokenSize(x) + 1)    // + 1 for `role`
                .reduce(Integer::sum)
                .orElse(0);
    }
}
