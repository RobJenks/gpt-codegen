package org.rj.modelgen.bpmn.models.generation.states;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
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
    private String intermediateModel;
    private BpmnModelInstance generatedBpmn;
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

        this.intermediateModel = input.getIntermediateModel();
        this.generatedBpmn = input.getGeneratedBpmn();
        this.bpmnValidationMessages = input.getBpmnValidationMessages();

        return terminalSignal();
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
