package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "value", "type" })
public class ElementNodeOutput {
    public enum Type { global, constant, code }

    private String name;
    private Type type;
    private String value;

    public ElementNodeOutput() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonIgnore
    public static ElementNodeOutput createConstant(String name, String value) {
        ElementNodeOutput output = new ElementNodeOutput();
        output.setName(name);
        output.setType(Type.constant);
        output.setValue(value);
        return output;
    }
}
