package org.rj.modelgen.llm.statemodel.signals.common;

import org.rj.modelgen.llm.state.ModelInterfaceInputPayload;
import org.rj.modelgen.llm.state.ModelInterfaceStartSignal;


/**
 * Generic signal to start a new model execution.  Requires only the default model execution payload
 */
public class StartModelExecutionSignal extends ModelInterfaceStartSignal<ModelInterfaceInputPayload> {

    public StartModelExecutionSignal(ModelInterfaceInputPayload inputPayload) {
        super("Start", inputPayload);
    }

    @Override
    public String getDescription() {
        return "Start a new model execution";
    }
}
