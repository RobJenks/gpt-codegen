package org.rj.modelgen.llm.state;

public class ModelInterfaceStateWithInputSignal<TSignal extends ModelInterfaceSignal> {
    private final ModelInterfaceState<TSignal> state;
    private final TSignal inputSignal;

    public ModelInterfaceStateWithInputSignal(ModelInterfaceState<TSignal> state, TSignal inputSignal) {
        this.state = state;
        this.inputSignal = inputSignal;
    }

    public ModelInterfaceState<TSignal> getState() {
        return state;
    }

    public TSignal getInputSignal() {
        return inputSignal;
    }
}
