package org.rj.modelgen.bpmn.models.generation;

public class BpmnGenerationExecutionModelOptions {
    private boolean useHistory = true;

    public BpmnGenerationExecutionModelOptions() { }

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
