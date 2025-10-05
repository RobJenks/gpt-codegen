package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.rj.modelgen.llm.intrep.graph.GraphNode;

import java.util.*;
import java.util.stream.Collectors;

public class ElementHighLevelNode implements GraphNode<String, String, ElementConnection> {
    private String id;
    private String name;
    private String elementType;
    private String description;
    private List<ElementConnection> connectedTo;
    private Map<String, Object> properties;
    private List<ElementHighLevelNodeInput> inputs;

    public ElementHighLevelNode() {
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

    public List<ElementHighLevelNodeInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ElementHighLevelNodeInput> inputs) {
        this.inputs = inputs;
    }

    public String generateSummary() {
        final var sb = new StringBuilder();

        sb.append(String.format("- Element \"%s\" has type \"%s\"", name, elementType));
        sb.append(String.format("- The element's purpose can be described as the following: \"%s\"", description));

        if (inputs != null && !inputs.isEmpty()) {
            sb.append(inputs.stream()
                    .map(ElementHighLevelNodeInput::generateSummary)
                    .map(x -> "  " + x)
                    .collect(Collectors.joining("\n", "\n", "\n")));
        }

        return sb.toString();
    }

}