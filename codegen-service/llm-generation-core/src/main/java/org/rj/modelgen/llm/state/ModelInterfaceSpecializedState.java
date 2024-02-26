package org.rj.modelgen.llm.state;


public abstract class ModelInterfaceSpecializedState<TInputSignal> extends ModelInterfaceState {
    public ModelInterfaceSpecializedState(Class<? extends ModelInterfaceState> cls) {
        this(cls, ModelInterfaceStateType.DEFAULT);
    }

    public ModelInterfaceSpecializedState(Class<? extends ModelInterfaceState> cls, ModelInterfaceStateType type) {
        super(cls, type);
    }

    // Unchecked, but guaranteed by type constraints on specialized state types
    @SuppressWarnings("unchecked")
    public TInputSignal asExpectedInputSignal(ModelInterfaceSignal signal) {
        return (TInputSignal)signal;
    }
}

