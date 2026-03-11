package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "source", "sourceType", "properties" })
public class ElementHighLevelNodeInput {
    private String name;
    private String source;
    private BpmnComponentInputSourceType sourceType;
    private List<ElementHighLevelNodeInput> properties;

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

    public List<ElementHighLevelNodeInput> getProperties() {
        return properties;
    }

    public void setProperties(List<ElementHighLevelNodeInput> properties) {
        this.properties = properties;
    }

    @JsonIgnore
    public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

    /*
        Serialization methods
     */

    public String generateSummary() {
        return generateSummary(0);
    }

    public String generateSummary(int indentLevel) {
        final String indent = "  ".repeat(indentLevel);

        if (hasProperties()) {
            final String childSummaries = properties.stream()
                    .map(prop -> prop.generateSummary(indentLevel + 1))
                    .collect(Collectors.joining("\n"));
            return String.format("%sInput \"%s\" is an object with properties:\n%s", indent, name, childSummaries);
        }
        return indent + switch (sourceType) {
            case NODE -> String.format("Input \"%s\" will be provided by node \"%s\"", name, source);
            case CONSTANT -> String.format("Input \"%s\" will be assigned constant value \"%s\"", name, source);
            case EXPRESSION -> String.format("Input \"%s\" will be assigned expression value \"%s\"", name, source);
            case GLOBAL -> String.format("Input \"%s\" will be assigned global value \"%s\"", name, source);
            case SCRIPT -> String.format("Input \"%s\" will be a Groovy script value \"%s\"", name, source);
        };
    }
}
