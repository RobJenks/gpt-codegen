package org.rj.modelgen.bpmn.component;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BpmnComponentLibraryDetailLevelSerializer implements ComponentLibrarySerializer<BpmnComponent> {
    @Override
    public String serialize(ComponentLibrary<BpmnComponent> library) {
        if (library == null) return "(Error: no component library available)";

        return Optional.ofNullable(library.getComponents())
                .orElseGet(List::of)
                .stream()
                .map(BpmnComponent::serializeDetailLevel)
                .collect(Collectors.joining("\n"));
    }
}
