package org.rj.modelgen.llm.models.generation.multilevel.prompt;

public enum MultiLevelModelPromptType {
    SanitizingPrePass,

    GenerateHighLevel,
    CorrectHighLevelSchemaErrors,

    GenerateDetailLevel,
    CorrectDetailLevelSchemaErrors,

    CorrectGeneratedModelErrors
}
