package org.rj.modelgen.llm.state;

public class ModelInterfaceStateWithInputSignal<TState extends ModelInterfaceState> {
    private final TState state;
    private final ModelInterfaceSignal<TState> inputSignal;

    public ModelInterfaceStateWithInputSignal(TState state, ModelInterfaceSignal<TState> inputSignal) {
        this.state = state;
        this.inputSignal = inputSignal;
    }

    public TState getState() {
        return state;
    }

    public ModelInterfaceSignal<TState> getInputSignal() {
        return inputSignal;
    }
}
