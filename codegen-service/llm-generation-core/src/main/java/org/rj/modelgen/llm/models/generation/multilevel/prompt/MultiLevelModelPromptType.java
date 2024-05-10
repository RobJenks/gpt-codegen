package org.rj.modelgen.llm.models.generation.multilevel.prompt;

public enum MultiLevelModelPromptType {
    GenerateHighLevel,
    CorrectHighLevelSchemaErrors,

    GenerateDetailLevel,
    CorrectDetailLevelSchemaErrors,

    CorrectGeneratedModelErrors
}
