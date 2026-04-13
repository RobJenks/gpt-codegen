package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.intrep.model.rendering.*;
import org.rj.modelgen.llm.intrep.graph.GraphNode;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "elementType",
        visible = true)
@JsonTypeIdResolver(ElementNodeTypeIdResolver.class)
public class ElementNode implements GraphNode<String, String, ElementConnection> {
    protected String id;
    protected String name;
    protected String elementType;
    protected String description;
    protected List<ElementConnection> connectedTo;
    protected Map<String, Object> properties;
    protected List<ElementNodeInput> inputs;
    protected List<ElementNodeOutput> outputs;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<ElementNodeInput> getInputs() {
        return inputs;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setInputs(List<ElementNodeInput> inputs) {
        this.inputs = inputs;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<ElementNodeOutput> getOutputs() {
        return outputs;
    }

    @JsonIgnore
    public ElementNodeOutput getOutputAt(int index) {
        if (outputs == null || index < 0 || index >= outputs.size()) {
            return null;
        }

        return outputs.get(index);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
        if (name == null || inputs == null) return Optional.empty();
        return getInputs().stream()
                .filter(input -> name.equals(input.getName()))
                .findFirst();
    }

    @JsonIgnore
    public List<ElementNodeInput> findAllInputs(String name) {
        if (name == null || inputs == null) return new ArrayList<>();
        return getInputs().stream()
                .filter(input -> name.equals(input.getName()))
                .toList();
    }

    @JsonIgnore
    public Optional<ElementNodeOutput> findOutput(String name) {
        if (name == null || outputs == null) return Optional.empty();
        return getOutputs().stream()
                .filter(outputs -> name.equals(outputs.getName()))
                .findFirst();
    }

    @JsonIgnore
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, BpmnComponent elementDefinition, String namespace) {
        return builder.manualTask(id).name(name).done();
    }

    @JsonIgnore
    protected String getAttrName(ElementNodeInput input, BpmnComponent elementDefinition) {
        return elementDefinition.getInputVariable(input.getName())
                .map(BpmnComponent.InputVariable::getAlias)
                .orElse(input.getName());
    }

    @JsonIgnore
    protected ExtensionElements getExtensionElements(BaseElement elementInstance) {
        ExtensionElements extensionElements = elementInstance.getExtensionElements();
        if (extensionElements == null) {
            extensionElements = elementInstance.getModelInstance().newInstance(ExtensionElements.class);
            elementInstance.setExtensionElements(extensionElements);
        }
        return extensionElements;
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
