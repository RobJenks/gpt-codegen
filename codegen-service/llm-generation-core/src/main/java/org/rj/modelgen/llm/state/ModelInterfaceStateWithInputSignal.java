package org.rj.modelgen.llm.state;

public class ModelInterfaceStateWithInputSignal {
    private final ModelInterfaceState state;
    private final ModelInterfaceSignal inputSignal;

    public ModelInterfaceStateWithInputSignal(ModelInterfaceState state, ModelInterfaceSignal inputSignal) {
        this.state = state;
        this.inputSignal = inputSignal;
    }

    public ModelInterfaceState getState() {
        return state;
    }

    public ModelInterfaceSignal getInputSignal() {
        return inputSignal;
    }
}
