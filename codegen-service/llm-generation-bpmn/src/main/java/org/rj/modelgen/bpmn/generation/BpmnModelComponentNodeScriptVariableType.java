package org.rj.modelgen.bpmn.generation;

public enum BpmnModelComponentNodeScriptVariableType {
    NODE,
    GLOBAL,
    OUTPUT,
    SCRIPT;

    @Override
    public String toString() {
        return name();
    }
}
