package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class LlmModelRequestPreparedSuccessfully extends ModelInterfaceSignal {
    private final String modelRequest;

    public LlmModelRequestPreparedSuccessfully(String modelRequest) {
        super(LlmModelRequestPreparedSuccessfully.class);
        this.modelRequest = modelRequest;
    }

    @Override
    public String getDescription() {
        return "Request for submission to LLM prepared successfully";
    }

    public String getModelRequest() {
        return modelRequest;
    }
}
