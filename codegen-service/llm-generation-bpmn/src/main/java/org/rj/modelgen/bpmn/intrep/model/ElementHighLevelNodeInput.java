package org.rj.modelgen.bpmn.intrep.model;

import org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType;

public class ElementHighLevelNodeInput {
    private String name;
    private String source;
    private BpmnComponentInputSourceType sourceType;

    public ElementHighLevelNodeInput() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BpmnComponentInputSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(BpmnComponentInputSourceType sourceType) {
        this.sourceType = sourceType;
    }

    /*
        Serialization methods
     */

    public String generateSummary() {
        return switch (sourceType) {
            case NODE -> String.format("Input \"%s\" will be provided by node \"%s\"", name, source);
            case CONSTANT -> String.format("Input \"%s\" will be assigned constant value \"%s\"", name, source);
            case EXPRESSION -> String.format("Input \"%s\" will be assigned expression value \"%s\"", name, source);
            case GLOBAL -> String.format("Input \"%s\" will be assigned global value \"%s\"", name, source);
            case SCRIPT -> String.format("Input \"%s\" will be a Groovy script value \"%s\"", name, source);
        };
    }
}
