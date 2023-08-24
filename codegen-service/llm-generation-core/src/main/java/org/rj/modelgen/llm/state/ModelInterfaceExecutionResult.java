package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Optional;

public class ModelInterfaceExecutionResult {
    private final ModelInterfaceState<? extends ModelInterfaceSignal> result;
    private List<ModelInterfaceStateWithInputSignal> executionPath;

    public ModelInterfaceExecutionResult(ModelInterfaceState<? extends ModelInterfaceSignal> result, List<ModelInterfaceStateWithInputSignal> executionPath) {
        this.result = result;
        this.executionPath = executionPath;
    }

    public ModelInterfaceState<? extends ModelInterfaceSignal> getResult() {
        return result;
    }

    public List<ModelInterfaceStateWithInputSignal> getExecutionPath() {
        return executionPath;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return Optional.ofNullable(result)
                .map(x -> ModelInterfaceStateType.TERMINAL_SUCCESS.equals(x.getType()))
                .orElse(false);
    }
}
