package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.beans.StateEvent;
import org.rj.modelgen.llm.beans.StateStatus;

public class ModelInterfaceStateEmittedSignal {
    private final ModelInterfaceState state;
    private final ModelInterfaceSignal signal;
    private final StateEvent stateEvent;
    private final StateStatus stateStatus;

    public ModelInterfaceStateEmittedSignal(ModelInterfaceState state, ModelInterfaceSignal signal, StateEvent stateEvent, StateStatus stateStatus) {
        this.state = state;
        this.signal = signal;
        this.stateEvent = stateEvent;
        this.stateStatus = stateStatus;
    }

    public ModelInterfaceState getState() {
        return state;
    }

    public ModelInterfaceSignal getSignal() {
        return signal;
    }

    public StateEvent getStateEvent() {
        return stateEvent;
    }

    public StateStatus getStateStatus() {
        return stateStatus;
    }
}
