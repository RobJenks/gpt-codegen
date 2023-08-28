package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlDataPassedValidation;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class BpmnGenerationComplete extends ModelInterfaceState<BpmnXmlDataPassedValidation> {
    private static final Logger LOG = LoggerFactory.getLogger(BpmnGenerationComplete.class);
    private String generatedBpmn = null;
    private List<String> modelValidationMessages = List.of();
    private List<String> bpmnValidationMessages = List.of();

    public BpmnGenerationComplete() {
        super(BpmnGenerationComplete.class);
    }

    @Override
    public String getDescription() {
        return "BPMN generation complete";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        this.generatedBpmn = input.getGeneratedBpmn();
        this.modelValidationMessages = input.getModelValidationMessages();
        this.bpmnValidationMessages = input.getBpmnValidationMessages();

        System.out.println("BPMN: " + generatedBpmn);
        System.out.println("Model validation: " + String.join(", ", modelValidationMessages));
        System.out.println("BPMN validation: " + String.join(", ", bpmnValidationMessages));

        return Mono.empty();
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
