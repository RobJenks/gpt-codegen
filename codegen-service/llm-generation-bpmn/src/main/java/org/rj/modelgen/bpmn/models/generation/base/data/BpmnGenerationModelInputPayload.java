package org.rj.modelgen.bpmn.models.generation.base.data;

import org.rj.modelgen.llm.state.ModelInterfaceInputPayload;

public class BpmnGenerationModelInputPayload extends ModelInterfaceInputPayload {
    public BpmnGenerationModelInputPayload(String sessionId, String request, String canvasModel) {
        super(sessionId, request, canvasModel);
    }
}
