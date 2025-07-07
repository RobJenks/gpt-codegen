package org.rj.modelgen.llm.models.generation.multilevel;

import org.rj.modelgen.llm.util.StringSerializable;

public enum MultiLevelGenerationModelStates implements StringSerializable {
    StartMultiLevelGeneration,
    SanitizingPrePass,
    PreProcessing,
    GenerateSubproblems,
    ExecuteHighLevel,
    ValidateHighLevel,
    ExecuteDetailLevel,
    ValidateDetailLevel,
    CombineSubproblems,
    PostProcessing,
    GenerateModel,
    Complete;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
