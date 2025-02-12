package org.rj.modelgen.llm.statemodel.data.common;

public enum StandardModelData {
    State,
    Signal,
    InputSignal,
    OutputSignal,
    SessionId,
    Request,
    Context,
    Llm,
    Temperature,
    Prompt,
    IntermediateModel,
    ModelResponse,
    ResponseContent,
    ValidationMessages,
    GeneratedModel,
    ModelValidationMessages;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
