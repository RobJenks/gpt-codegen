package org.rj.modelgen.llm.state;

public abstract class GenerationComplete extends ModelInterfaceState {
    public GenerationComplete(Class<? extends GenerationComplete> cls) {
        super(cls, ModelInterfaceStateType.TERMINAL_SUCCESS);
    }
}
