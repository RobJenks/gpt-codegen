package org.rj.modelgen.llm.models.generation.multilevel.prompt;

public enum MultiLevelModelPromptType {
    PrePass1,

    GenerateHighLevel,
    CorrectHighLevelSchemaErrors,

    GenerateDetailLevel,
    CorrectDetailLevelSchemaErrors,

    CorrectGeneratedModelErrors
}
