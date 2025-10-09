package org.rj.modelgen.bpmn.intrep.model;

public class ElementHighLevelNodeInput {
    private String name;
    private String source;
    private ElementHighLevelNodeInputSourceType sourceType;

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

    public ElementHighLevelNodeInputSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ElementHighLevelNodeInputSourceType sourceType) {
        this.sourceType = sourceType;
    }

    /*
        Serialization methods
     */

    public String generateSummary() {
        return switch (sourceType) {
            case NODE -> String.format("Input \"%s\" will be provided by node \"%s\"", name, source);
            case CONSTANT -> String.format("Input \"%s\" will be assigned constant value \"%s\"", name, source);
            case GLOBAL -> String.format("Input \"%s\" will be assigned global value \"%s\"", name, source);
        };
    }
}
