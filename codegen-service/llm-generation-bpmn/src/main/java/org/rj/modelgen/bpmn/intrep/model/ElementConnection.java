package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rj.modelgen.llm.intrep.graph.GraphConnection;

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
}
