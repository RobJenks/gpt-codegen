package org.rj.modelgen.bpmn.models.generation.signals;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;

public class LlmModelRequestPreparedSuccessfully extends ModelInterfaceSignal {
    private final Context context;

    public LlmModelRequestPreparedSuccessfully(Context context) {
        super(LlmModelRequestPreparedSuccessfully.class);
        this.context = context;
    }

    @Override
    public String getDescription() {
        return "Request for submission to LLM prepared successfully";
    }

    public Context getContext() {
        return context;
    }
}
