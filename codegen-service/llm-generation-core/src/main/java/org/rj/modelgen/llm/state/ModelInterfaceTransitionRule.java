package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Transition rule { (CurrentState, OutputSignal) -> NextState }
 */

public class ModelInterfaceTransitionRule {
    private final ModelInterfaceState currentState;
    private final String outputSignalId;
    private final ModelInterfaceState nextState;

    public <E extends Enum<E>> ModelInterfaceTransitionRule(ModelInterfaceState currentState, E outputSignalId, ModelInterfaceState nextState) {
        this(currentState, outputSignalId.name(), nextState);
    }

    public ModelInterfaceTransitionRule(ModelInterfaceState currentState, String outputSignalId, ModelInterfaceState nextState) {
        this.currentState = currentState;
        this.outputSignalId = outputSignalId;
        this.nextState = nextState;
    }

    public ModelInterfaceState getCurrentState() {
        return currentState;
    }

    public String getOutputSignalId() {
        return outputSignalId;
    }

    public ModelInterfaceState getNextState() {
        return nextState;
    }

    @Override
    public String toString() {
        return String.format("TransitionRule ((%s, %s) -> %s)",
                currentState != null ? currentState.getId() : "<null>",
                outputSignalId != null ? outputSignalId : "<null>",
                nextState != null ? nextState.getId() : "<null>");
    }

    @JsonIgnore
    public boolean matches(ModelInterfaceState currentState, String outputSignalId) {
        return  this.currentState.isSameStateType(currentState) &&
                this.outputSignalId.equals(outputSignalId);
    }

    @JsonIgnore
    public boolean isValid() {
        return  currentState != null &&
                outputSignalId != null &&
                nextState != null;
    }
}
