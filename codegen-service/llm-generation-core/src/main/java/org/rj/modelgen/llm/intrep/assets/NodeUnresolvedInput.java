package org.rj.modelgen.llm.intrep.assets;

import org.rj.modelgen.llm.component.ComponentInputResolutionStrategy;

public class NodeUnresolvedInput {

    private String nodeId;
    private String type;
    private String value;
    private ComponentInputResolutionStrategy resolutionStrategy;

    public NodeUnresolvedInput() { }

    public NodeUnresolvedInput(String nodeId, String type, String value, ComponentInputResolutionStrategy resolutionStrategy) {
        this.nodeId = nodeId;
        this.type = type;
        this.value = value;
        this.resolutionStrategy = resolutionStrategy;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ComponentInputResolutionStrategy getResolutionStrategy() {
        return resolutionStrategy;
    }

    public void setResolutionStrategy(ComponentInputResolutionStrategy resolutionStrategy) {
        this.resolutionStrategy = resolutionStrategy;
    }
}
