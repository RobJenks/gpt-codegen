package org.rj.modelgen.bpmn.models.generation.states;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class BpmnGenerationComplete extends ModelInterfaceState {
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

        this.intermediateModel = getPayload().get(StandardModelData.IntermediateModel);
        this.generatedBpmn = getPayload().get(StandardModelData.GeneratedBpmn);
        this.bpmnValidationMessages = getPayload().get(StandardModelData.BpmnValidationMessages);

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
