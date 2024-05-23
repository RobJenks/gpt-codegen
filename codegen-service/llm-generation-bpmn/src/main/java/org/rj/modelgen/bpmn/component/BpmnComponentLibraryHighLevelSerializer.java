package org.rj.modelgen.bpmn.component;

import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BpmnComponentLibraryHighLevelSerializer implements ComponentLibrarySerializer<BpmnComponentLibrary> {
    @Override
    public String serialize(BpmnComponentLibrary library) {
        if (library == null) return "(Error: no component library available)";

        return Optional.ofNullable(library.getComponents())
                .orElseGet(List::of)
                .stream()
                .map(BpmnComponent::serializeHighLevel)
                .collect(Collectors.joining("\n"));
    }
}
