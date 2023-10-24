package org.rj.modelgen.service.beans;

public class BpmnGenerationSessionData {
    private final String id;
    private String currentIntermediateRepData;
    private String currentBpmnData;

    public BpmnGenerationSessionData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCurrentIntermediateRepData() {
        return currentIntermediateRepData;
    }

    public void setCurrentIntermediateRepData(String currentIntermediateRepData) {
        this.currentIntermediateRepData = currentIntermediateRepData;
    }

    public String getCurrentBpmnData() {
        return currentBpmnData;
    }

    public void setCurrentBpmnData(String currentBpmnData) {
        this.currentBpmnData = currentBpmnData;
    }
}
