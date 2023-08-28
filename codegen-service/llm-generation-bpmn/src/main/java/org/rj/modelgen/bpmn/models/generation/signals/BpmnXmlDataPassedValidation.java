package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class BpmnXmlDataPassedValidation extends ModelInterfaceSignal {
    private final String generatedBpmn;
    private final List<String> modelValidationMessages;
    private final List<String> bpmnValidationMessages;

    public BpmnXmlDataPassedValidation(String generatedBpmn, List<String> modelValidationMessages, List<String> bpmnValidationMessages) {
        super(BpmnXmlDataPassedValidation.class);
        this.generatedBpmn = generatedBpmn;
        this.modelValidationMessages = modelValidationMessages;
        this.bpmnValidationMessages = bpmnValidationMessages;
    }

    @Override
    public String getDescription() {
        return "BPMN XML data passed all validations";
    }

    public String getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getModelValidationMessages() {
        return modelValidationMessages;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }
}
