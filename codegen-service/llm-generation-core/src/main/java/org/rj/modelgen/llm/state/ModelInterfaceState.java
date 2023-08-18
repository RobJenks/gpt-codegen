package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public abstract class ModelInterfaceState {
    private final String id;
    private int invokeCount;
    private Integer invokeLimit;

    public ModelInterfaceState(String id) {
        this.id = id;

        this.invokeCount = 0;
        this.invokeLimit = null;        // Limit is set if non-null
    }

    public String getId() {
        return id;
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

    /**
     * Called by the model interface state machine when entering the new state.  Performs some basic operations
     * before delegating to subclasses for all action logic
     *
     * @param inputSignal       Signal received from the previous state
     * @return                  Output signal containing the result of this action
     */
    @JsonIgnore
    public ModelInterfaceSignal invoke(ModelInterfaceSignal inputSignal) {
        this.invokeCount += 1;
        if (hasInvokeLimit() && invokeCount > invokeLimit) {
            return new ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS(id, invokeCount);
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
    public abstract ModelInterfaceSignal invokeAction(ModelInterfaceSignal inputSignal);
}
