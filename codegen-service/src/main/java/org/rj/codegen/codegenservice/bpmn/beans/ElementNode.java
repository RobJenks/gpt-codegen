package org.rj.codegen.codegenservice.bpmn.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElementNode {
    private String id;
    private String name;
    private String elementType;
    private String description;
    private List<Connection> connectedTo;
    private Map<String, String> properties;

    public ElementNode() {
    }

    public ElementNode(String id, String name, String elementType) {
        this.id = id;
        this.name = name;
        this.elementType = elementType;
    }

    public String getId() {
        return id;
    }

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

    public List<Connection> getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(List<Connection> connectedTo) {
        this.connectedTo = connectedTo;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static class Connection {
        private String targetNode;
        private String description;

        public Connection() { }

        public Connection(String targetNode, String description) {
            this.targetNode = targetNode;
            this.description = description;
        }

        public String getTargetNode() {
            return targetNode;
        }

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
}
