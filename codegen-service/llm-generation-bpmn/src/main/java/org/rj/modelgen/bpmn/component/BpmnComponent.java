package org.rj.modelgen.bpmn.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.bpmn.component.common.BpmnComponentVariableType;
import org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType;
import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentInputResolutionStrategy;

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
            boolean shouldIncludeConstants = options.shouldIncludeConstantInputs();

            if (!mandatory.isEmpty()) {
                result.append("Mandatory inputs: \n")
                        .append(serializeInputs(mandatory, 0, shouldIncludeConstants))
                        .append('\n');
            }

            if (!options.isMandatoryInputsOnly() && !optional.isEmpty()) {
                result.append("Optional inputs: \n")
                        .append(serializeInputs(optional, 0, shouldIncludeConstants))
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

    private String serializeInputs(List<InputVariable> inputs, int level, boolean shouldIncludeConstants) {
        if (inputs == null || inputs.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (InputVariable input : inputs) {
            if (input == null) continue;

            var sourceTypes = input.getAllowedInputSourceTypes();
            if (!shouldIncludeConstants && sourceTypes != null && sourceTypes.size() == 1 && sourceTypes.get(0).equals(BpmnComponentInputSourceType.CONSTANT)) {
                continue;
            }

            final String indent = " ".repeat(level * 4);
            final String bullet = (level == 0) ? "- " : "* ";
            String singleInput = indent + bullet + input.getName() + " (" + buildInlineInputDetails(input) + ")";
            sb.append(singleInput);

            final List<InputVariable> props = input.getProperties();
            if (props != null && !props.isEmpty()) {
                sb.append(" Object properties:\n")
                        .append(serializeInputs(props, level + 1, shouldIncludeConstants));
            }
            sb.append('\n');
        }

        // trim last newline
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String buildInlineInputDetails(InputVariable input) {
        final List<String> components = new ArrayList<>();

        components.add(input.isMandatory() || input.isKeyValue() ? "Mandatory" : "Optional");

        if (input.getType() != null) {
            components.add("Type: " + input.getType().name());
        }
        if (StringUtils.isNotBlank(input.getDescription())) {
            components.add(input.getDescription().trim());
        }
        if (input.getAllowedValues() != null && !input.getAllowedValues().isEmpty()) {
            components.add("Allowed values: " + String.join(", ", input.getAllowedValues()));
        }
        if (input.getAllowedInputSourceTypes() != null && !input.getAllowedInputSourceTypes().isEmpty()) {
            components.add("Allowed input source types: " + input.getAllowedInputSourceTypes().stream()
                    .map(BpmnComponentInputSourceType::toString)
                    .collect(Collectors.joining(", ")));
        }

        return String.join("; ", components);
    }

    @Override
    public String defaultSerialize() {
        return generateSerializedPromptData(BpmnComponentLibrarySerializationOptions.defaultOptions());
    }

    @JsonIgnore
    public Optional<InputVariable> getInputVariable(String inputVarName) {
        if (StringUtils.isBlank(inputVarName)) {
            return Optional.empty();
        }
        return findInputVariable(requiredInputs, inputVarName);
    }

    private Optional<InputVariable> findInputVariable(List<InputVariable> inputs, String inputVarName) {
        if (inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }

        for (InputVariable input : inputs) {
            if (input == null) continue;
            if (inputVarName.equals(input.getName())) {
                return Optional.of(input);
            }
            var nestedInput = findInputVariable(input.getProperties(), inputVarName);
            if (nestedInput.isPresent()) {
                return nestedInput;
            }
        }
        return Optional.empty();
    }

    public static class Variable {
        private String name;
        private String description;
        private BpmnComponentVariableType type;
        private boolean keyValue;
        private String resolveValue;

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

        public String getResolveValue() {
            return resolveValue;
        }

        public void setResolveValue(String resolveValue) {
            this.resolveValue = resolveValue;
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
        private String defaultValue;
        private String alias;
        private List<InputVariable> properties;
        private List<String> allowedValues;
        private List<BpmnComponentInputSourceType> allowedInputSourceTypes;
        private ComponentInputResolutionStrategy resolutionStrategy;

        public InputVariable() {
            this(null, null);
        }

        public InputVariable(String name, String description) {
            this(name, description, BpmnComponentVariableType.String, false, null, null, List.of(), List.of(), List.of(), null);
        }

        public InputVariable(String name, String description, BpmnComponentVariableType type, boolean mandatory, String defaultValue, String alias,
                             List<InputVariable> properties, List<String> allowedValues, List<BpmnComponentInputSourceType> allowedInputSourceTypes,
                            ComponentInputResolutionStrategy resolutionStrategy) {
            super(name, description, type);
            this.mandatory = mandatory;
            this.defaultValue = defaultValue;
            this.alias = alias;
            this.properties = properties;
            this.allowedValues = allowedValues;
            this.allowedInputSourceTypes = allowedInputSourceTypes;
            this.resolutionStrategy = resolutionStrategy;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public List<InputVariable> getProperties() {
            return properties;
        }

        public void setProperties(List<InputVariable> properties) {
            this.properties = properties;
        }

        public List<String> getAllowedValues() {
            return allowedValues;
        }

        public void setAllowedValues(List<String> allowedValues) {
            this.allowedValues = allowedValues;
        }

        public List<BpmnComponentInputSourceType> getAllowedInputSourceTypes() {
            return allowedInputSourceTypes;
        }

        public void setAllowedInputSourceTypes(List<BpmnComponentInputSourceType> allowedInputType) {
            this.allowedInputSourceTypes = allowedInputType;
        }

        public Boolean isAllowedInputSourceType(String type) {
            if (allowedInputSourceTypes == null || allowedInputSourceTypes.isEmpty()) {
                return true;
            }
            return allowedInputSourceTypes.stream()
                    .anyMatch(t -> t.name().equalsIgnoreCase(type));
        }

        public ComponentInputResolutionStrategy getResolutionStrategy() {
            return resolutionStrategy;
        }

        public void setResolutionStrategy(ComponentInputResolutionStrategy resolutionStrategy) {
            this.resolutionStrategy = resolutionStrategy;
        }
    }
}
