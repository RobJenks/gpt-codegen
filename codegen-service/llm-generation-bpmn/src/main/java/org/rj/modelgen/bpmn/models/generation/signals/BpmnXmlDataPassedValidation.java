package org.rj.modelgen.bpmn.models.generation.signals;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class BpmnXmlDataPassedValidation extends ModelInterfaceSignal {
    private final String intermediateModel;
    private final BpmnModelInstance generatedBpmn;
    private final List<String> bpmnValidationMessages;

    public BpmnXmlDataPassedValidation(String intermediateModel, BpmnModelInstance generatedBpmn, List<String> bpmnValidationMessages) {
        super(BpmnGenerationSignals.CompleteGeneration);
        this.intermediateModel = intermediateModel;
        this.generatedBpmn = generatedBpmn;
        this.bpmnValidationMessages = bpmnValidationMessages;
    }

    @Override
    public String getDescription() {
        return "BPMN XML data passed all validations";
    }

    public String getIntermediateModel() {
        return intermediateModel;
    }

    public BpmnModelInstance getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }
}
