package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class LlmResponseReceived extends ModelInterfaceSignal {
    private final String modelResponse;

    public LlmResponseReceived(String modelResponse) {
        super(LlmResponseReceived.class);
        this.modelResponse = modelResponse;
    }

    @Override
    public String getDescription() {
        return "Valid response returned by LLM";
    }

    public String getModelResponse() {
        return modelResponse;
    }
}
