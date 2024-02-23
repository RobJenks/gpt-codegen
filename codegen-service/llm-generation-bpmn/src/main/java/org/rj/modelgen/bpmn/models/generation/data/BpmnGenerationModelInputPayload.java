package org.rj.modelgen.bpmn.models.generation.data;

import org.rj.modelgen.llm.state.ModelInterfaceDataPayload;

public class BpmnGenerationModelInputPayload extends ModelInterfaceDataPayload {
    public BpmnGenerationModelInputPayload(String sessionId, String request) {
        super(sessionId, request);
    }
}
