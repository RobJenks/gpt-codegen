package org.rj.modelgen.bpmn.models.generation;

import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;

public class BpmnGenerationExecutionModelOptions extends GenerationModelOptionsImpl<BpmnGenerationExecutionModelOptions> {
    private boolean useHistory = true;

    public BpmnGenerationExecutionModelOptions() {
        super();
    }

    public static BpmnGenerationExecutionModelOptions defaultOptions() {
        return new BpmnGenerationExecutionModelOptions();
    }

    public boolean shouldUseHistory() {
        return useHistory;
    }

    public void setUseHistory(boolean useHistory) {
        this.useHistory = useHistory;
    }

    public BpmnGenerationExecutionModelOptions withUseHistory(boolean useHistory) {
        setUseHistory(useHistory);
        return this;
    }
}
