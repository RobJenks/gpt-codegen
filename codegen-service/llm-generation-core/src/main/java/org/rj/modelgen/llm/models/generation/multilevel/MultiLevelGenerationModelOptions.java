package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;

public class MultiLevelGenerationModelOptions {
    private MultiLevelGenerationModelPromptGenerator promptGeneratorOverride;
    private ModelSchema highLevelSchemaOverride;
    private ModelSchema detailLevelSchemaOverride;

    private MultiLevelGenerationModelOptions() { }

    public static MultiLevelGenerationModelOptions defaultOptions() {
        return new MultiLevelGenerationModelOptions();
    }

    public MultiLevelGenerationModelPromptGenerator getPromptGeneratorOverride() {
        return promptGeneratorOverride;
    }

    public void setPromptGeneratorOverride(MultiLevelGenerationModelPromptGenerator promptGeneratorOverride) {
        this.promptGeneratorOverride = promptGeneratorOverride;
    }

    public MultiLevelGenerationModelOptions withPromptGeneratorOverride(MultiLevelGenerationModelPromptGenerator promptGeneratorOverride) {
        setPromptGeneratorOverride(promptGeneratorOverride);
        return this;
    }

    public ModelSchema getHighLevelSchemaOverride() {
        return highLevelSchemaOverride;
    }

    public void setHighLevelSchemaOverride(ModelSchema highLevelSchemaOverride) {
        this.highLevelSchemaOverride = highLevelSchemaOverride;
    }

    public MultiLevelGenerationModelOptions withHighLevelSchemaOverride(ModelSchema highLevelSchemaOverride) {
        setHighLevelSchemaOverride(highLevelSchemaOverride);
        return this;
    }

    public ModelSchema getDetailLevelSchemaOverride() {
        return detailLevelSchemaOverride;
    }

    public void setDetailLevelSchemaOverride(ModelSchema detailLevelSchemaOverride) {
        this.detailLevelSchemaOverride = detailLevelSchemaOverride;
    }

    public MultiLevelGenerationModelOptions withDetailLevelSchemaOverride(ModelSchema detailLevelSchemaOverride) {
        setDetailLevelSchemaOverride(detailLevelSchemaOverride);
        return this;
    }
}
