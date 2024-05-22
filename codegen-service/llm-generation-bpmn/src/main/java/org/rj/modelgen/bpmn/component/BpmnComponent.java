package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.component.Component;

public class BpmnComponent extends Component {
    private String name;
    private String description;
    private String usage;

    public BpmnComponent() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    @JsonIgnore
    public String serializeHighLevel() {
        return String.format("Name: %s\nDescription: %s\nUsage: %s", name, description, usage);
    }

    @JsonIgnore
    public String serializeDetailLevel() {
        return String.format("Name: %s\nDescription: %s\nUsage: %s", name, description, usage);  // TODO
    }
}
