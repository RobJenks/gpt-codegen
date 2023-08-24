package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Transition rule { (CurrentState, OutputSignal) -> NextState }
 */
public class ModelInterfaceTransitionRule<TTargetState extends ModelInterfaceState> {
    private final ModelInterfaceState currentState;
    private final ModelInterfaceSignal<TTargetState> outputSignal;
    private final TTargetState nextState;

    public ModelInterfaceTransitionRule(ModelInterfaceState currentState, ModelInterfaceSignal<TTargetState> outputSignal, TTargetState nextState) {
        this.currentState = currentState;
        this.outputSignal = outputSignal;
        this.nextState = nextState;
    }

    public ModelInterfaceState getCurrentState() {
        return currentState;
    }

    public ModelInterfaceSignal<TTargetState> getOutputSignal() {
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
    public <TSignalTarget extends ModelInterfaceState>
    boolean matches(ModelInterfaceState currentState, ModelInterfaceSignal<TSignalTarget> outputSignal) {
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
