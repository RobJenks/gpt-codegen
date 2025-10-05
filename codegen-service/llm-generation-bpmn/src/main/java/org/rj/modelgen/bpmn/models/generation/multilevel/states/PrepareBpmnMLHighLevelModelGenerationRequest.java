package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;

public class PrepareBpmnMLHighLevelModelGenerationRequest<TComponentLibrary extends ComponentLibrary<?>>
        extends PrepareBpmnMLModelGenerationRequest<TComponentLibrary> {

    public PrepareBpmnMLHighLevelModelGenerationRequest(PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, ?, ?, ?> params, BpmnGlobalVariableLibrary globalVariableLibrary) {
        super(params, globalVariableLibrary);
    }
}
