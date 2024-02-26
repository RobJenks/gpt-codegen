package org.rj.modelgen.bpmn.intrep.bpmn.model;

import org.rj.modelgen.llm.intrep.graph.GraphNode;

import java.util.*;

public class ElementNode implements GraphNode<String, ElementConnection> {
    private String id;
    private String name;
    private String elementType;
    private String description;
    private List<ElementConnection> connectedTo;
    private Map<String, String> properties;

    public ElementNode() {
    }

    public ElementNode(String id, String name, String elementType) {
        this.id = id;
        this.name = name;
        this.elementType = elementType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Collection<ElementConnection> getConnectedTo() {
        return connectedTo;
    }

    @Override
    public void setConnectedTo(Collection<ElementConnection> connections) {
        this.connectedTo = Optional.ofNullable(connections).map(ArrayList::new).orElse(null);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
