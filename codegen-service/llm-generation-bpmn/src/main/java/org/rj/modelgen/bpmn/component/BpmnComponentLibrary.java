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

    public static BpmnComponentLibrary fromResource(String resource) {
        return fromResource(resource, BpmnComponentLibrary.class);
    }

    public static <T extends BpmnComponentLibrary> BpmnComponentLibrary fromResource(String resource, Class<T> libraryClass) {
        return fromSerialized(Util.loadStringResource(resource), libraryClass);
    }

    public static BpmnComponentLibrary fromSerialized(String serialized) {
        return fromSerialized(serialized, BpmnComponentLibrary.class);
    }

    public static <T extends BpmnComponentLibrary> T fromSerialized(String serialized, Class<T> libraryClass) {
        return Util.deserializeOrThrow(serialized, libraryClass,
                e -> new RuntimeException("Failed to deserialize BPMN component library into '%s': %s"
                        .formatted(libraryClass != null ? libraryClass.getSimpleName() : "<null>", e.getMessage()), e));
    }

}
