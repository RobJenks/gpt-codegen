package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Transition rule { (CurrentState, OutputSignal) -> NextState }
 */

public class ModelInterfaceTransitionRule<TSignal extends ModelInterfaceSignal> {
    private final ModelInterfaceState<? extends ModelInterfaceSignal> currentState;
    private final Class<TSignal> outputSignal;
    private final ModelInterfaceState<TSignal> nextState;

    public ModelInterfaceTransitionRule(ModelInterfaceState<? extends ModelInterfaceSignal> currentState,
                                        Class<TSignal> outputSignal, ModelInterfaceState<TSignal> nextState) {
        this.currentState = currentState;
        this.outputSignal = outputSignal;
        this.nextState = nextState;
    }

    public ModelInterfaceState<? extends ModelInterfaceSignal> getCurrentState() {
        return currentState;
    }

    public Class<TSignal> getOutputSignal() {
        return outputSignal;
    }

    public ModelInterfaceState<TSignal> getNextState() {
        return nextState;
    }

    @Override
    public String toString() {
        return String.format("TransitionRule ((%s, %s) -> %s)",
                currentState != null ? currentState.getId() : "<null>",
                outputSignal != null ? outputSignal.getName() : "<null>",
                nextState != null ? nextState.getId() : "<null>");
    }

    @JsonIgnore
    public boolean matches(ModelInterfaceState<? extends ModelInterfaceSignal> currentState, ModelInterfaceSignal outputSignal) {
        return  this.currentState.isSameStateType(currentState) &&
                this.outputSignal.equals(outputSignal.getClass());
    }

    @JsonIgnore
    public boolean isValid() {
        return  currentState != null &&
                outputSignal != null &&
                nextState != null;
    }
}
