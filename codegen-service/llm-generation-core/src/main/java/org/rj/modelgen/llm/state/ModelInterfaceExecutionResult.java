package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Optional;

public class ModelInterfaceExecutionResult<TResult extends ModelInterfaceState> {
    private final TResult result;
    private final List<ModelInterfaceStateWithInputSignal> executionPath;

    public ModelInterfaceExecutionResult(TResult result, List<ModelInterfaceStateWithInputSignal> executionPath) {
        this.result = result;
        this.executionPath = executionPath;
    }

    public TResult getResult() {
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
