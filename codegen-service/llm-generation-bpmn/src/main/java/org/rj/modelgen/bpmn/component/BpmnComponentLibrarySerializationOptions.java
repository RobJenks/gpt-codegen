package org.rj.modelgen.bpmn.component;

public class BpmnComponentLibrarySerializationOptions {

    // TODO: add BPMN serialization options
    private boolean placeholderOption = true;

    public BpmnComponentLibrarySerializationOptions() { }

    public static BpmnComponentLibrarySerializationOptions defaultOptions() {
        return new BpmnComponentLibrarySerializationOptions();
    }
    
    public boolean isPlaceholderOption() {
        return placeholderOption;
    }
    
    public void setPlaceholderOption(boolean placeholderOption) {
        this.placeholderOption = placeholderOption;
    }
    
    public BpmnComponentLibrarySerializationOptions withPlaceholderOption(boolean placeholderOption) {
        this.placeholderOption = placeholderOption;
        return this;
    }
}
