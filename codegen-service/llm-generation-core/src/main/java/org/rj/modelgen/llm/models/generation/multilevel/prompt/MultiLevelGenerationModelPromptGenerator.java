package org.rj.modelgen.llm.models.generation.multilevel.prompt;

import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;

public class MultiLevelGenerationModelPromptGenerator extends TemplatedPromptGenerator<MultiLevelGenerationModelPromptGenerator, MultiLevelModelPromptType> {
    public static MultiLevelGenerationModelPromptGenerator create(String prePass1Prompt,
                                                                  String highLevelGenerationPrompt,
                                                                  String highLevelSchemaCorrectionPrompt,
                                                                  String detailLevelGenerationPrompt,
                                                                  String detailLevelSchemaCorrectionPrompt,
                                                                  String generatedModelErrorCorrectionPrompt) {
        return new MultiLevelGenerationModelPromptGenerator()
                .withAvailablePrompt(MultiLevelModelPromptType.SanitizingPrePass, prePass1Prompt)
                .withAvailablePrompt(MultiLevelModelPromptType.GenerateHighLevel, highLevelGenerationPrompt)
                .withAvailablePrompt(MultiLevelModelPromptType.CorrectHighLevelSchemaErrors, highLevelSchemaCorrectionPrompt)
                .withAvailablePrompt(MultiLevelModelPromptType.GenerateDetailLevel, detailLevelGenerationPrompt)
                .withAvailablePrompt(MultiLevelModelPromptType.CorrectDetailLevelSchemaErrors, detailLevelSchemaCorrectionPrompt)
                .withAvailablePrompt(MultiLevelModelPromptType.CorrectGeneratedModelErrors, generatedModelErrorCorrectionPrompt);
    }

    public MultiLevelGenerationModelPromptGenerator() {
        super();
    }
}
