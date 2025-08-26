package org.rj.modelgen.bpmn.component.synthetic;

import org.rj.modelgen.llm.util.StringSerializable;

public enum BpmnSyntheticElementType implements StringSerializable {
    UnknownElement(BpmnSyntheticElementNode.class);

    private final Class<? extends BpmnSyntheticElementNode> elementClass;

    BpmnSyntheticElementType(Class<? extends BpmnSyntheticElementNode> elementClass) {
        this.elementClass = elementClass;
    }

    public Class<? extends BpmnSyntheticElementNode> getElementClass() {
        return elementClass;
    }

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
