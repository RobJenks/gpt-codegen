package org.rj.modelgen.bpmn.intrep.model.assets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.component.ComponentInputResolutionStrategy;
import org.rj.modelgen.llm.intrep.assets.NodeUnresolvedInput;

public class ElementNodeUnresolvedInput extends NodeUnresolvedInput {

    private String name;
    private String defaultValue;
    private String path;
    private String message;

    public ElementNodeUnresolvedInput() { }

    public ElementNodeUnresolvedInput(String nodeId, String elementType, String name, String value, String defaultValue, String path, ComponentInputResolutionStrategy resolutionStrategy) {
        super(nodeId, elementType, value, resolutionStrategy);
        this.name = name;
        this.defaultValue = defaultValue;
        this.path = path;
        this.message = constructMessage();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public String constructMessage() {
        return switch (getResolutionStrategy()) {
            case USER_REQUIRED -> String.format("Input '%s' is required for element '%s' (%s) but missing in your process description and could not be inferred from context. Please provide a value.", path, getNodeId(), getType());
            case INFERRED_CONFIRM -> String.format(
                    "Input '%s' is required for element '%s' (%s) but is missing in your process description, therefore it was " +
                            (defaultValue != null
                                    ? "defaulted to '" + defaultValue
                                    : "inferred from context to be '" + getValue()) +
                            "'. Please review the provided value.",
                    path, getNodeId(), getType());
            default -> "";// Not needed for other resolution strategies, as they will be filled in with inferred values by LLM
        };
    }
}
