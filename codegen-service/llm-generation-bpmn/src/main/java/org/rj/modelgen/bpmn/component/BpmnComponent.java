package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.component.Component;

public class BpmnComponent extends Component {
    private String name;
    private String description;

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

    @JsonIgnore
    public String serializeHighLevel() {
        return String.format("%s: %s", name, description);
    }

    @JsonIgnore
    public String serializeDetailLevel() {
        return String.format("%s: %s", name, description);  // TODO
    }
}
