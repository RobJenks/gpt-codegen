package org.rj.codegen.codegenservice.bpmn.beans;

import java.util.Map;

public class ElementNode {
    private String id;
    private String name;
    private String type;
    private Map<String, String> properties;

    public ElementNode() {
    }

    public ElementNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
