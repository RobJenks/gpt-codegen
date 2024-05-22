package org.rj.modelgen.bpmn.models.generation.base.data;

import org.rj.modelgen.llm.state.ModelInterfaceInputPayload;

public class BpmnGenerationModelInputPayload extends ModelInterfaceInputPayload {
    public BpmnGenerationModelInputPayload(String sessionId, String request) {
        super(sessionId, request);
    }
}
