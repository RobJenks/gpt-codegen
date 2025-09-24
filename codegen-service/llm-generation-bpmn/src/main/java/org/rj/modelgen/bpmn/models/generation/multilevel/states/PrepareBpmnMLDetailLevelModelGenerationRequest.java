package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;

public class PrepareBpmnMLDetailLevelModelGenerationRequest<TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareBpmnMLModelGenerationRequest<TComponentLibrary> {

    public PrepareBpmnMLDetailLevelModelGenerationRequest(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, ?, ?, ?> params) {
        super(params);
    }
}
