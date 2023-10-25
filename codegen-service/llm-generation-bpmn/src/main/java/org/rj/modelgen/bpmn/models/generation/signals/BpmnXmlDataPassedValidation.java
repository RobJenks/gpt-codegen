package org.rj.modelgen.bpmn.models.generation.signals;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class BpmnXmlDataPassedValidation extends ModelInterfaceSignal {
    private final BpmnModelInstance generatedBpmn;
    private final List<String> bpmnValidationMessages;

    public BpmnXmlDataPassedValidation(BpmnModelInstance generatedBpmn, List<String> bpmnValidationMessages) {
        super(BpmnXmlDataPassedValidation.class);
        this.generatedBpmn = generatedBpmn;
        this.bpmnValidationMessages = bpmnValidationMessages;
    }

    @Override
    public String getDescription() {
        return "BPMN XML data passed all validations";
    }

    public BpmnModelInstance getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }
}
