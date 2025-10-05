package org.rj.modelgen.bpmn.component.globalvars.library;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.component.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BpmnGlobalVariable extends Component {
    private String name;
    private boolean available;
    private String description;
    private String type;

    public BpmnGlobalVariable() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String defaultSerialize() {
        if (name == null) return "<null>";

        final String typeString = (type == null ? null : "type: " + type);
        final String additionalInfo = Stream.of(description, typeString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

        return name + (StringUtils.isEmpty(additionalInfo) ? "" : " (" + additionalInfo + ")");
    }
}
