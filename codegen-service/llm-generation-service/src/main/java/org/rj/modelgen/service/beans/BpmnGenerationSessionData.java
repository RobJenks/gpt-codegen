package org.rj.modelgen.service.beans;

public class BpmnGenerationSessionData {
    private final String id;
    private String currentIntermediateModelData;
    private String currentBpmnData;

    public BpmnGenerationSessionData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCurrentIntermediateModelData() {
        return currentIntermediateModelData;
    }

    public void setCurrentIntermediateModelData(String currentIntermediateModelData) {
        this.currentIntermediateModelData = currentIntermediateModelData;
    }

    public String getCurrentBpmnData() {
        return currentBpmnData;
    }

    public void setCurrentBpmnData(String currentBpmnData) {
        this.currentBpmnData = currentBpmnData;
    }
}
