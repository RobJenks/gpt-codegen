package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.component.ComponentLibrary;

import java.util.List;
import java.util.stream.Collectors;

public class BpmnComponentLibrary extends ComponentLibrary<BpmnComponent> {
    public BpmnComponentLibrary() {
        super();
    }

    public BpmnComponentLibrary(List<BpmnComponent> bpmnComponents) {
        super(bpmnComponents);
    }

    @JsonIgnore
    public String serializeHighLevel() {
        return getComponents().stream()
                .map(BpmnComponent::serializeHighLevel)
                .collect(Collectors.joining("\n"));
    }

    public static BpmnComponentLibrary defaultLibrary() {
        return new BpmnComponentLibrary();  // TODO
    }
}
