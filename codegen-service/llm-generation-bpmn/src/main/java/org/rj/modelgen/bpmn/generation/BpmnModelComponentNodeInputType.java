package org.rj.modelgen.bpmn.generation;

public enum BpmnModelComponentNodeInputType {
    CONSTANT,
    COMPONENT,
    GLOBAL;

    @Override
    public String toString() {
        return name();
    }
}
