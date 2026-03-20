package org.rj.modelgen.bpmn.component.globalvars.library;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.component.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BpmnGlobalVariable extends Component {
    private String name;
    private String description;
    private String type;
    private List<GlobalVariableArgument> arguments;
    private String resolveValue;
    private String initScript;

    public BpmnGlobalVariable() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<GlobalVariableArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<GlobalVariableArgument> arguments) {
        this.arguments = arguments;
    }

    public String getResolveValue() {
        return resolveValue;
    }

    public void setResolveValue(String resolveValue) {
        this.resolveValue = resolveValue;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    @Override
    public String defaultSerialize() {
        if (name == null) return "<null>";

        String argumentsInfo = null;
        if (arguments != null && !arguments.isEmpty()) {
            String argsList = arguments.stream()
                    .map(arg -> arg.getType() + " " + arg.getName() + " - " + arg.getDescription())
                    .collect(Collectors.joining(", "));
            argumentsInfo = "Required arguments: " + argsList;
        }

        final String typeString = (type == null ? null : "type: " + type);
        final String additionalInfo = Stream.of(description, typeString, argumentsInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

        return " - " + name + (StringUtils.isEmpty(additionalInfo) ? "" : " (" + additionalInfo + ")");
    }

    public static class GlobalVariableArgument {
        private String name;
        private String type;
        private String description;

        public GlobalVariableArgument() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
