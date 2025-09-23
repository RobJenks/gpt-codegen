package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;

public class PrepareBpmnMLHighLevelModelGenerationRequest<TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareBpmnMLModelGenerationRequest<TComponentLibrary> {

    public PrepareBpmnMLHighLevelModelGenerationRequest(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, ?, ?> params) {
        super(params);
    }
}
