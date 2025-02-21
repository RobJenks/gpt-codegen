package org.rj.modelgen.llm.models.generation.multilevel.prompt;

public enum MultiLevelModelPromptType {
    SanitizingPrePass,

    PreProcessing,

    GenerateHighLevel,
    CorrectHighLevelSchemaErrors,

    GenerateDetailLevel,
    CorrectDetailLevelSchemaErrors,

    CorrectGeneratedModelErrors,

    PostProcessing
}
