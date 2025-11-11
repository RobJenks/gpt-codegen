package org.rj.modelgen.bpmn.generation;

public enum BpmnModelComponentNodeInputType {
    CONSTANT,
    SCRIPT,
    NODE,
    GLOBAL;

    @Override
    public String toString() {
        return name();
    }
}
