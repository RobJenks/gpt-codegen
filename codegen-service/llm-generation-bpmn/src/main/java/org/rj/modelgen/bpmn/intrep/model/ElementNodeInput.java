package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Optional;

import static org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "value", "variableSource", "isProvided", "properties" })
public class ElementNodeInput {
    private String name;
    private String value;
    private String variableSource;
    private List<ElementNodeInput> properties;
    private Boolean isProvided;

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

    public List<ElementNodeInput> getProperties() {
        return properties;
    }

    public void setProperties(List<ElementNodeInput> properties) {
        this.properties = properties;
    }

    public Boolean getIsProvided() {
        return isProvided;
    }

    public void setIsProvided(Boolean isProvided) {
        this.isProvided = isProvided;
    }

    @JsonIgnore
    public Boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

    @JsonIgnore
    public Optional<ElementNodeInput> findProperty(String name) {
        if (properties == null || name == null) {
            return Optional.empty();
        }
        return properties.stream()
                .filter(prop -> prop.getName().equals(name))
                .findFirst();
    }

    @JsonIgnore
    public String findPropertyValueOrDefault(String name, String defaultValue) {
        if (properties == null || name == null) {
            return defaultValue;
        }
        return properties.stream()
                .filter(prop -> prop.getName().equals(name))
                .findFirst()
                .map(ElementNodeInput::getValue)
                .orElse(defaultValue);
    }

    @JsonIgnore
    public List<ElementNodeInput> findAllProperties(String name) {
        if (properties == null || name == null) {
            return List.of();
        }
        return properties.stream()
                .filter(prop -> prop.getName().equals(name))
                .toList();
    }

    @JsonIgnore
    public static ElementNodeInput createConstant(String name, String value) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(CONSTANT.toString());
        input.setValue(value);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createExpression(String name, String value) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(EXPRESSION.toString());
        input.setValue(value);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createScript(String name, String value) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(SCRIPT.toString());
        input.setValue(value);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createGlobal(String name, String globalVariable) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(GLOBAL.toString());
        input.setValue(globalVariable);
        return input;
    }

    @JsonIgnore
    public static ElementNodeInput createNodeSourced(String name, String sourceNodeName) {
        ElementNodeInput input = new ElementNodeInput();
        input.setName(name);
        input.setVariableSource(NODE.toString());
        input.setValue(sourceNodeName);
        return input;
    }
}
