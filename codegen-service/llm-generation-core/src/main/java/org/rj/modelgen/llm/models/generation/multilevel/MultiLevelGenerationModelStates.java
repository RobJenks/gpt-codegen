package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.util.StringSerializable;

public enum MultiLevelGenerationModelStates implements StringSerializable {
    StartMultiLevelGeneration,
    SanitizingPrePass,
    PreProcessing,
    ExecuteHighLevel,
    ValidateHighLevel,
    ExecuteDetailLevel,
    ValidateDetailLevel,
    PostProcessing,
    GenerateModel,
    Complete;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
