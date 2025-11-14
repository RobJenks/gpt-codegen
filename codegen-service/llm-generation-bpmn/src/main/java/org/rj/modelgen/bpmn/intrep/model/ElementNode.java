package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.rj.modelgen.llm.intrep.graph.GraphNode;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElementNode implements GraphNode<String, String, ElementConnection> {
    private String id;
    private String name;
    private String elementType;
    private String description;
    private List<ElementConnection> connectedTo;
    private Map<String, Object> properties;
    private List<ElementNodeInput> inputs;
    private List<ElementNodeOutput> outputs;

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

    @Override
    public String getName() {
        return name;
    }

    @Override
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ElementNodeInput> getInputs() {
        if (inputs == null) {
            return Collections.emptyList();
        }
        return inputs;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public void setInputs(List<ElementNodeInput> inputs) {
        this.inputs = inputs;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ElementNodeOutput> getOutputs() {
        if (outputs == null) {
            return Collections.emptyList();
        }
        return outputs;
    }

    @JsonIgnore
    public ElementNodeOutput getOutputAt(int index) {
        if (outputs == null || index < 0 || index >= outputs.size()) {
            return null;
        }

        return outputs.get(index);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public void setOutputs(List<ElementNodeOutput> outputs) {
        this.outputs = outputs;
    }

    @JsonIgnore
    public boolean setOutputAt(int index, ElementNodeOutput output) {
        if (outputs == null || index < 0 || index >= outputs.size()) {
            return false;
        }

        outputs.set(index, output);
        return true;
    }

    /* Convenience methods */

    @JsonIgnore
    public Optional<ElementNodeInput> findInput(String name) {
        if (name == null) return Optional.empty();
        return getInputs().stream()
                .filter(input -> name.equals(input.getName()))
                .findFirst();
    }

    @JsonIgnore
    public Optional<ElementNodeOutput> findOutput(String name) {
        if (name == null) return Optional.empty();
        return getOutputs().stream()
                .filter(outputs -> name.equals(outputs.getName()))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementNode that = (ElementNode) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(connectedTo, that.connectedTo) &&
                Objects.equals(properties, that.properties) &&
                Objects.equals(inputs, that.inputs) &&
                Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, elementType, description, connectedTo, properties, inputs, outputs);
    }
}
