package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.util.Util;

import java.util.List;
import java.util.stream.Collectors;

public class BpmnComponentLibrary extends ComponentLibrary<BpmnComponent> {
    private List<BpmnComponent> components;

    public BpmnComponentLibrary() {
        super();
    }

    public BpmnComponentLibrary(List<BpmnComponent> bpmnComponents) {
        super(bpmnComponents);
    }

    @Override
    public ComponentLibrary<BpmnComponent> constructEmpty() {
        return new BpmnComponentLibrary();
    }

    @Override
    public List<BpmnComponent> getComponents() {
        return components;
    }

    @Override
    public void setComponents(List<BpmnComponent> components) {
        this.components = components;
    }

    @JsonIgnore
    public String serializeHighLevel() {
        return getComponents().stream()
                .map(BpmnComponent::serializeHighLevel)
                .collect(Collectors.joining("\n---\n"));
    }

    public static BpmnComponentLibrary defaultLibrary() {
        return fromResource("content/components/bpmn-component-library.json");
    }

    public static BpmnComponentLibrary fromSerialized(String serialized) {
        return Util.deserializeOrThrow(serialized, BpmnComponentLibrary.class,
                e -> new RuntimeException("Failed to deserialize BPMN component library: " + e.getMessage(), e));
    }

    public static BpmnComponentLibrary fromResource(String resource) {
        return fromSerialized(Util.loadStringResource(resource));
    }
}
