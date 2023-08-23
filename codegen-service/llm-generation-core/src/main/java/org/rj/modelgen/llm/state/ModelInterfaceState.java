package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import reactor.core.publisher.Mono;

import java.util.Objects;

public abstract class ModelInterfaceState {
    private final String id;
    private final ModelInterfaceStateType type;
    private int invokeCount;
    private Integer invokeLimit;

    public ModelInterfaceState(String id) {
        this(id, ModelInterfaceStateType.DEFAULT);
    }

    public ModelInterfaceState(String id, ModelInterfaceStateType type) {
        this.id = id;
        this.type = type;

        this.invokeCount = 0;
        this.invokeLimit = null;        // Limit is set if non-null
    }

    public String getId() {
        return id;
    }

    public ModelInterfaceStateType getType() {
        return type;
    }

    /**
     * Implemented by subclasses; return a text description of the state
     */
    @JsonIgnore
    public abstract String getDescription();

    @JsonIgnore
    public boolean isSameStateType(ModelInterfaceState otherState) {
        if (otherState == null) return false;
        return Objects.equals(id, otherState.id);
    }

    public int getInvokeCount() {
        return invokeCount;
    }

    public Integer getInvokeLimit() {
        return invokeLimit;
    }

    @JsonIgnore
    public boolean hasInvokeLimit() {
        return invokeLimit != null;
    }

    public void setInvokeLimit(Integer invokeLimit) {
        this.invokeLimit = invokeLimit;
    }

    public boolean isTerminal() {
        return  type == ModelInterfaceStateType.TERMINAL_SUCCESS ||
                type == ModelInterfaceStateType.TERMINAL_FAILURE;
    }

    /**
     * Called by the model interface state machine when entering the new state.  Performs some basic operations
     * before delegating to subclasses for all action logic
     *
     * @param inputSignal       Signal received from the previous state
     * @return                  Output signal containing the result of this action
     */
    @JsonIgnore
    public Mono<ModelInterfaceSignal> invoke(ModelInterfaceSignal inputSignal) {
        this.invokeCount += 1;
        if (hasInvokeLimit() && invokeCount > invokeLimit) {
            return Mono.just(new ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS(id, invokeCount));
        }

        return invokeAction(inputSignal);
    }

    /**
     * Implemented by subclasses.  Perform all actions attached to this state, based on the input signal
     * received from the previous state, and output a new signal containing the results of this action
     *
     * @param inputSignal       Signal received from the previous state
     * @return                  Output signal containing the result of this action
     */
    @JsonIgnore
    protected abstract Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal);
}
