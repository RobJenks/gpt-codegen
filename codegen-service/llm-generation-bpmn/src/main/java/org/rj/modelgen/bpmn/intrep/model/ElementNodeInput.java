package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.rj.modelgen.bpmn.generation.BpmnModelComponentNodeInputType;

@JsonPropertyOrder({ "name", "value", "variableSource" })
public class ElementNodeInput {
    private String name;
    private String value;
    private String variableSource;

    public ElementNodeInput() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getVariableSource() {
        return variableSource;
    }

    public void setVariableSource(String variableSource) {
        this.variableSource = variableSource;
    }

    @JsonIgnore
    public static ElementNodeInput createConstant(String name, String value) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(BpmnModelComponentNodeInputType.CONSTANT.toString());
        input.setValue(value);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createScript(String name, String value) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(BpmnModelComponentNodeInputType.SCRIPT.toString());
        input.setValue(value);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createGlobal(String name, String globalVariable) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(BpmnModelComponentNodeInputType.GLOBAL.toString());
        input.setValue(globalVariable);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createNodeSourced(String name, String sourceNodeName, String sourceNodeOutputName) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(sourceNodeName);
        input.setValue(sourceNodeOutputName);
        return input;
    }
}
