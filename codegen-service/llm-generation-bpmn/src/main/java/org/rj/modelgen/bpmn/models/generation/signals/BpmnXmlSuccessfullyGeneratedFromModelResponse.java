package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class BpmnXmlSuccessfullyGeneratedFromModelResponse extends ModelInterfaceSignal {
    private final String generatedBpmn;
    private final List<String> modelValidationMessages;
    public BpmnXmlSuccessfullyGeneratedFromModelResponse(String generatedBpmn, List<String> modelValidationMessages) {
        super(BpmnXmlSuccessfullyGeneratedFromModelResponse.class);
        this.generatedBpmn = generatedBpmn;
        this.modelValidationMessages = modelValidationMessages;
    }

    @Override
    public String getDescription() {
        return "BPMN XML data was successfully generated for model data returned by the LLM";
    }

    public String getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getModelValidationMessages() {
        return modelValidationMessages;
    }
}
