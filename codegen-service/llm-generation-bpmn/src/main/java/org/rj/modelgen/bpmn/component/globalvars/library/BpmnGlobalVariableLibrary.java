package org.rj.modelgen.bpmn.component.globalvars.library;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.util.Util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BpmnGlobalVariableLibrary extends ComponentLibrary<BpmnGlobalVariable> {

    private List<BpmnGlobalVariable> variables;
    private Boolean returnFilteredVars = false;

    public BpmnGlobalVariableLibrary() {
        this(List.of());
    }

    public BpmnGlobalVariableLibrary(List<BpmnGlobalVariable> variables) {
        super(variables);
    }

    public static BpmnGlobalVariableLibrary fromResource(String resource) {
        final var content = Util.loadStringResource(resource);
        return Util.deserializeOrThrow(content, BpmnGlobalVariableLibrary.class);
    }

    public static BpmnGlobalVariableLibrary defaultLibrary() {
        return fromResource("content/components/bpmn-global-variable-library.json");
    }

    public static BpmnGlobalVariableLibrary empty() {
        return new BpmnGlobalVariableLibrary();
    }

    @Override
    public ComponentLibrary<BpmnGlobalVariable> constructEmpty() {
        return empty();
    }

    @Override
    public List<BpmnGlobalVariable> getComponents() {
        return variables;
    }

    @Override
    public void setComponents(List<BpmnGlobalVariable> variables) {
        this.variables = variables;
    }


    @JsonIgnore
    public Optional<BpmnGlobalVariable> getVariableByName(String name) {
        return variables.stream()
                .filter(variable -> variable.getName().equals(name))
                .findFirst();
    }

    @JsonIgnore
    public String defaultSerialize() {
        return variables.stream()
                .filter(BpmnGlobalVariable::isAvailable)
                .map(BpmnGlobalVariable::defaultSerialize)
                .collect(Collectors.joining("\n"));
    }

    @JsonIgnore
    public BpmnGlobalVariableLibrary getFilteredBasedOnPrompt(String prompt) {
        if (prompt == null) return BpmnGlobalVariableLibrary.empty();
        return (BpmnGlobalVariableLibrary) getFilteredLibrary(var -> prompt.contains(var.getName()));
    }

    @JsonIgnore
    public JSONObject toJson() {
        return new JSONObject(Util.serializeOrThrow(this,
                e -> new RuntimeException("Unable to serialize global variable library: " + e.getMessage(), e)));
    }
}
