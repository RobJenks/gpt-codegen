package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;
import org.rj.modelgen.llm.schema.ModelSchema;


public class MultiLevelGenerationModelOptions extends GenerationModelOptionsImpl<MultiLevelGenerationModelOptions> {
    private ModelSchema highLevelSchemaOverride;
    private ModelSchema detailLevelSchemaOverride;
    private boolean performSubproblemDecomposition = false;

    protected MultiLevelGenerationModelOptions() { }

    public static MultiLevelGenerationModelOptions defaultOptions() {
        return new MultiLevelGenerationModelOptions();
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

    public boolean shouldPerformSubproblemDecomposition() {
        return performSubproblemDecomposition;
    }

    public void setPerformSubproblemDecomposition(boolean performSubproblemDecomposition) {
        this.performSubproblemDecomposition = performSubproblemDecomposition;
    }

    public MultiLevelGenerationModelOptions withPerformSubproblemDecomposition(boolean performSubproblemDecomposition) {
        setPerformSubproblemDecomposition(performSubproblemDecomposition);
        return this;
    }
}
