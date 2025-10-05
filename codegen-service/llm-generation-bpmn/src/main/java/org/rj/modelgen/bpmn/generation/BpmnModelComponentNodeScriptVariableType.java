package org.rj.modelgen.bpmn.generation;

public enum BpmnModelComponentNodeScriptVariableType {
    NODE,
    GLOBAL,
    OUTPUT;

    @Override
    public String toString() {
        return name();
    }
}
