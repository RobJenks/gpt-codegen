package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Transition rule { (CurrentState, OutputSignal) -> NextState }
 */
public class ModelInterfaceTransitionRule {
    private final ModelInterfaceState currentState;
    private final ModelInterfaceSignal outputSignal;
    private final ModelInterfaceState nextState;

    public ModelInterfaceTransitionRule(ModelInterfaceState currentState, ModelInterfaceSignal outputSignal, ModelInterfaceState nextState) {
        this.currentState = currentState;
        this.outputSignal = outputSignal;
        this.nextState = nextState;
    }

    public ModelInterfaceState getCurrentState() {
        return currentState;
    }

    public ModelInterfaceSignal getOutputSignal() {
        return outputSignal;
    }

    public ModelInterfaceState getNextState() {
        return nextState;
    }

    @Override
    public String toString() {
        return String.format("TransitionRule ((%s, %s) -> %s)",
                currentState != null ? currentState.getId() : "<null>",
                outputSignal != null ? outputSignal.getId() : "<null>",
                nextState != null ? nextState.getId() : "<null>");
    }

    @JsonIgnore
    public boolean matches(ModelInterfaceState currentState, ModelInterfaceSignal outputSignal) {
        return  this.currentState.isSameStateType(currentState) &&
                this.outputSignal.isSameSignalType(outputSignal);
    }

    @JsonIgnore
    public boolean isValid() {
        return  currentState != null &&
                outputSignal != null &&
                nextState != null;
    }
}
