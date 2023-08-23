package org.rj.modelgen.llm.state;

public class ModelInterfaceStateEmittedSignal {
    private final ModelInterfaceState state;
    private final ModelInterfaceSignal signal;

    public ModelInterfaceStateEmittedSignal(ModelInterfaceState state, ModelInterfaceSignal signal) {
        this.state = state;
        this.signal = signal;
    }

    public ModelInterfaceState getState() {
        return state;
    }

    public ModelInterfaceSignal getSignal() {
        return signal;
    }
}
