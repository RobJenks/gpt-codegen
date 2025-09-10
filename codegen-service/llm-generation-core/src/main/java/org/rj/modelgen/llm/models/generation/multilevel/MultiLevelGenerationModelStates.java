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

    public String description()
    {
        return switch (this) {
            case StartMultiLevelGeneration -> "Starting multi-level workflow generation process";
            case SanitizingPrePass -> "Sanitizing provided input";
            case PreProcessing -> "Pre-processing provided input";
            case GenerateSubproblems -> "Decomposing the input into sub-problems";
            case ExecuteHighLevel -> "Generating high-level intermediate model";
            case ValidateHighLevel -> "Validating high-level intermediate model";
            case ExecuteDetailLevel -> "Executing detail-level intermediate model";
            case ValidateDetailLevel -> "Validating detail-level intermediate model";
            case CombineSubproblems -> "Combining results from sub-problems";
            case PostProcessing -> "Processing final intermediate model";
            case GenerateModel -> "Generating final workflow";
            case Complete -> "Multi-level generation process complete";
        };
    }
}
