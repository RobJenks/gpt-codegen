package org.rj.modelgen.bpmn.component;

import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

public class BpmnComponentLibraryHighLevelSerializer implements ComponentLibrarySerializer<BpmnComponentLibrary> {

    private final BpmnComponentLibrarySerializationOptions options = BpmnComponentLibrarySerializationOptions.defaultOptions()
            .withIncludeInputs(true)
            .withIncludeOutputs(true)
            .withMandatoryInputsOnly(false);

    @Override
    public String serialize(BpmnComponentLibrary library) {
        if (library == null) return "(Error: no component library available)";

        return library.generateSerializedPromptData(options);
    }
}
