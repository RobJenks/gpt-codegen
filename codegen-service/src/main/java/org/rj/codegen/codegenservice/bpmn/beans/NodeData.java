package org.rj.codegen.codegenservice.bpmn.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.codegen.codegenservice.util.Util;

import java.util.ArrayList;
import java.util.List;

public class NodeData {
    private List<ElementNode> elements = new ArrayList<>();
    private List<ConnectionNode> connections = new ArrayList<>();

    public NodeData() {
    }

    public List<ElementNode> getElements() {
        return elements;
    }

    public void setElements(List<ElementNode> elements) {
        this.elements = elements;
    }

    public List<ConnectionNode> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionNode> connections) {
        this.connections = connections;
    }

    @JsonIgnore
    public String serialize() {
        return Util.serializeOrThrow(this);
    }
}
