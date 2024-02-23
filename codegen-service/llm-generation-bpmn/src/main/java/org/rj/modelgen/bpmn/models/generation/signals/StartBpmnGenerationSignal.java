package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.bpmn.models.generation.data.BpmnGenerationModelInputPayload;
import org.rj.modelgen.llm.state.ModelInterfaceStartSignal;


public class StartBpmnGenerationSignal extends ModelInterfaceStartSignal<BpmnGenerationModelInputPayload> {

    public StartBpmnGenerationSignal(BpmnGenerationModelInputPayload payload) {
        super(StartBpmnGenerationSignal.class, payload);
    }

    @Override
    public String getDescription() {
        return "Start a new BPMN generation process";
    }
}
