package org.rj.modelgen.llm.state;

public enum ModelInterfaceStateType {
    // Default intermediate node in the model
    DEFAULT,

    // Terminal node; model has ended in success
    TERMINAL_SUCCESS,

    // Terminal state; model has ended in failure, e.g. exception, timeout, no matching transition rule
    TERMINAL_FAILURE
}
