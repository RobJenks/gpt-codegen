package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.rj.modelgen.bpmn.component.common.BpmnComponentVariableType;
import org.rj.modelgen.llm.component.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BpmnComponent extends Component {
    private String name;
    private String description;
    private String usage;
    private List<InputVariable> requiredInputs;
    private List<Variable> generatedOutputs;

    public BpmnComponent() { }

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

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public List<InputVariable> getRequiredInputs() {
        return requiredInputs;
    }

    public void setRequiredInputs(List<InputVariable> requiredInputs) {
        this.requiredInputs = requiredInputs;
    }

    public List<Variable> getGeneratedOutputs() {
        return generatedOutputs;
    }

    public void setGeneratedOutputs(List<Variable> generatedOutputs) {
        this.generatedOutputs = generatedOutputs;
    }

    @JsonIgnore
    public String generateSerializedPromptData(BpmnComponentLibrarySerializationOptions options) {
        final var result = new StringBuilder(String.format("Name: %s\nDescription: %s\nUsage: %s\n", name, description, usage));

        if (options.shouldIncludeInputs()) {
            final Map<Boolean, List<InputVariable>> inputs = Optional.ofNullable(requiredInputs).orElseGet(List::of).stream()
                    .collect(Collectors.partitioningBy(i -> (i.isMandatory() || i.isKeyValue())));
            final var mandatory = inputs.get(Boolean.TRUE);
            final var optional = inputs.get(Boolean.FALSE);

            if (!mandatory.isEmpty()) {
                result.append("Mandatory inputs: \n").append(mandatory.stream()
                                .map(InputVariable::getVariableSummary)
                                .collect(Collectors.joining(",\n")))
                        .append('\n');
            }

            if (!options.isMandatoryInputsOnly() && !optional.isEmpty()) {
                result.append("Optional inputs: \n").append(optional.stream()
                                .map(InputVariable::getVariableSummary)
                                .collect(Collectors.joining(",\n ")))
                        .append('\n');
            }
        }

        if (options.shouldIncludeOutputs()) {
            if (generatedOutputs != null && !generatedOutputs.isEmpty()) {
                result.append("Automatically-generated outputs: \n").append(generatedOutputs.stream()
                                .map(Variable::getVariableSummary)
                                .collect(Collectors.joining(",\n")))
                        .append('\n');
            }
        }

        return result.toString().trim();
    }

    @Override
    public String defaultSerialize() {
        return generateSerializedPromptData(BpmnComponentLibrarySerializationOptions.defaultOptions());
    }

    public static class Variable {
        private String name;
        private String description;
        private BpmnComponentVariableType type;
        private boolean keyValue;

        public Variable() {
            this(null, null);
        }

        public Variable(String name, String description) {
            this(name, description, BpmnComponentVariableType.String);
        }

        public Variable(String name, String description, BpmnComponentVariableType type) {
            this.name = name;
            this.description = description;
            this.type = Optional.ofNullable(type).orElse(BpmnComponentVariableType.String);
        }

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

        @JsonInclude()
        public BpmnComponentVariableType getType() {
            return type;
        }

        public void setType(BpmnComponentVariableType type) {
            this.type = type;
        }

        public boolean isKeyValue() {
            return keyValue;
        }

        public void setKeyValue(boolean keyValue) {
            this.keyValue = keyValue;
        }

        protected List<String> buildSummaryComponents() {
            final List<String> components = new ArrayList<>();
            if (type != null && type != BpmnComponentVariableType.String) {
                components.add(type.name());
            }
            if (description != null) {
                components.add(description);
            }
            return components;
        }

        @JsonIgnore
        public String getVariableSummary() {
            final var components = buildSummaryComponents();
            if (components.isEmpty()) {
                return name;
            }
            return String.format("  - %s (%s)", name, String.join("; ", components));
        }
    }

    public static class InputVariable extends Variable {
        private boolean mandatory;
        private List<String> allowedValues;

        public InputVariable() {
            this(null, null);
        }

        public InputVariable(String name, String description) {
            this(name, description, BpmnComponentVariableType.String, false, List.of());
        }

        public InputVariable(String name, String description, BpmnComponentVariableType type, boolean mandatory, List<String> allowedValues) {
            super(name, description, type);
            this.mandatory = mandatory;
            this.allowedValues = allowedValues;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public List<String> getAllowedValues() {
            return allowedValues;
        }

        public void setAllowedValues(List<String> allowedValues) {
            this.allowedValues = allowedValues;
        }

        @Override
        protected List<String> buildSummaryComponents() {
            final List<String> components = super.buildSummaryComponents();
            if (allowedValues != null && !allowedValues.isEmpty()) {
                components.add("Allowed values: " + String.join(", ", allowedValues));
            }
            return components;
        }

        @JsonIgnore
        public String getVariableSummary() {
            return super.getVariableSummary();
        }
    }
}
