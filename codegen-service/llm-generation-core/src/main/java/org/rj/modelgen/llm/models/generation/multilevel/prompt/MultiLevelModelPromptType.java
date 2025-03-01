package org.rj.modelgen.llm.models.generation.multilevel.prompt;

import org.rj.modelgen.llm.util.StringSerializable;

public enum MultiLevelModelPromptType implements StringSerializable {
    SanitizingPrePass,

    PreProcessing,

    GenerateHighLevel,
    CorrectHighLevelSchemaErrors,

    GenerateDetailLevel,
    CorrectDetailLevelSchemaErrors,

    CorrectGeneratedModelErrors,

    PostProcessing;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
