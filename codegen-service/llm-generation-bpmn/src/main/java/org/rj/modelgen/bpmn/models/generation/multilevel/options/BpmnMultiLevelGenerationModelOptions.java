package org.rj.modelgen.bpmn.models.generation.multilevel.options;

import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModelOptions;

public class BpmnMultiLevelGenerationModelOptions extends MultiLevelGenerationModelOptions {
    private boolean addPlaceholderForUnknownComponents = false;

    protected BpmnMultiLevelGenerationModelOptions() {
        super();
    }

    public static BpmnMultiLevelGenerationModelOptions defaultOptions() {
        return new BpmnMultiLevelGenerationModelOptions();
    }

    public boolean shouldAddPlaceholderForUnknownComponents() {
        return addPlaceholderForUnknownComponents;
    }

    public void setAddPlaceholderForUnknownComponent(boolean addPlaceholderForUnknownComponents) {
        this.addPlaceholderForUnknownComponents = addPlaceholderForUnknownComponents;
    }

    public BpmnMultiLevelGenerationModelOptions withAddPlaceholderForUnknownComponents(boolean addPlaceholderForUnknownComponents) {
        setAddPlaceholderForUnknownComponent(addPlaceholderForUnknownComponents);
        return this;
    }
}
