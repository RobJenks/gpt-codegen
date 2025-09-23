package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rj.modelgen.llm.intrep.graph.GraphConnection;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElementConnection implements GraphConnection<String> {
    private String targetNode;
    private String description;

    public ElementConnection() { }

    public ElementConnection(String targetNode, String description) {
        this.targetNode = targetNode;
        this.description = description;
    }

    @Override
    public String getTargetNode() {
        return targetNode;
    }

    @Override
    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementConnection that = (ElementConnection) o;
        return Objects.equals(targetNode, that.targetNode) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetNode, description);
    }
}
