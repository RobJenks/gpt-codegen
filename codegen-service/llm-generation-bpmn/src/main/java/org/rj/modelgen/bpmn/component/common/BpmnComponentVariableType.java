package org.rj.modelgen.bpmn.component.common;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum BpmnComponentVariableType {
    String,
    Integer,
    Float,
    Boolean;
    
    public static Optional<BpmnComponentVariableType> parse(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(name))
                .findFirst();
    }

    @JsonValue
    @Override
    public java.lang.String toString() {
        return name().toLowerCase();
    }
}
