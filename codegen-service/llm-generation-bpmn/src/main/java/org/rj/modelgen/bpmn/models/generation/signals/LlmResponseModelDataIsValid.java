package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;

import java.util.List;

public class LlmResponseModelDataIsValid extends ModelInterfaceSignal {
    private final String modelResponse;
    private final List<String> validationMessages;

    public LlmResponseModelDataIsValid(String modelResponse, List<String> validationMessages) {
        super(LlmResponseModelDataIsValid.class);
        this.modelResponse = modelResponse;
        this.validationMessages = validationMessages;
    }

    @Override
    public String getDescription() {
        return "Model data returned in the LLM response is valid";
    }

    public String getModelResponse() {
        return modelResponse;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }
}
