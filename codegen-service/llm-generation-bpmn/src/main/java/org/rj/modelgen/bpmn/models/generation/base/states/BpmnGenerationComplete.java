package org.rj.modelgen.bpmn.models.generation.base.states;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.assets.BpmnIntermediateModelAssets;
import org.rj.modelgen.llm.state.GenerationComplete;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import reactor.core.publisher.Mono;

import java.util.List;

public class BpmnGenerationComplete extends GenerationComplete {
    private BpmnIntermediateModel intermediateModel;
    private BpmnIntermediateModelAssets intermediateModelAssets;
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
        this.intermediateModelAssets = getPayload().get(StandardModelData.IntermediateModelAssets);
        this.generatedBpmn = getPayload().get(StandardModelData.GeneratedModel);
        this.bpmnValidationMessages = getPayload().get(StandardModelData.ModelValidationMessages);

        return terminalSignal();
    }

    public BpmnIntermediateModel getIntermediateModel() {
        return intermediateModel;
    }

    public BpmnIntermediateModelAssets getIntermediateModelAssets() {
        return intermediateModelAssets;
    }

    public BpmnModelInstance getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }
}
