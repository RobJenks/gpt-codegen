package org.rj.modelgen.bpmn.component;

public class BpmnComponentLibrarySerializationOptions {

    private boolean mandatoryInputsOnly = false;
    private boolean includeInputs = true;
    private boolean includeOutputs = true;

    public BpmnComponentLibrarySerializationOptions() { }

    public static BpmnComponentLibrarySerializationOptions defaultOptions() {
        return new BpmnComponentLibrarySerializationOptions();
    }

    public boolean isMandatoryInputsOnly() {
        return mandatoryInputsOnly;
    }

    public void setMandatoryInputsOnly(boolean mandatoryInputsOnly) {
        this.mandatoryInputsOnly = mandatoryInputsOnly;
    }

    public BpmnComponentLibrarySerializationOptions withMandatoryInputsOnly(boolean mandatoryInputsOnly) {
        setMandatoryInputsOnly(mandatoryInputsOnly);
        return this;
    }

    public boolean shouldIncludeInputs() {
        return includeInputs;
    }

    public void setIncludeInputs(boolean includeInputs) {
        this.includeInputs = includeInputs;
    }

    public BpmnComponentLibrarySerializationOptions withIncludeInputs(boolean includeInputs) {
        setIncludeInputs(includeInputs);
        return this;
    }

    public boolean shouldIncludeOutputs() {
        return includeOutputs;
    }

    public void setIncludeOutputs(boolean includeOutputs) {
        this.includeOutputs = includeOutputs;
    }

    public BpmnComponentLibrarySerializationOptions withIncludeOutputs(boolean includeOutputs) {
        setIncludeOutputs(includeOutputs);
        return this;
    }
}
