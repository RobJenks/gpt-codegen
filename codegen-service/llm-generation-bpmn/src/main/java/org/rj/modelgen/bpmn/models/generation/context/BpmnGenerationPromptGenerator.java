package org.rj.modelgen.bpmn.models.generation.context;

import org.rj.modelgen.llm.prompt.PromptGenerator;

public class BpmnGenerationPromptGenerator extends PromptGenerator<BpmnGenerationPromptGenerator, BpmnGenerationPromptType> {
    public static BpmnGenerationPromptGenerator create(String generationPrompt,
                                                       String schemaErrorCorrectionPrompt,
                                                       String bpmnErrorCorrectionPrompt) {
        return new BpmnGenerationPromptGenerator()
                .withAvailablePrompt(BpmnGenerationPromptType.Generate, generationPrompt)
                .withAvailablePrompt(BpmnGenerationPromptType.CorrectSchemaErrors, schemaErrorCorrectionPrompt)
                .withAvailablePrompt(BpmnGenerationPromptType.CorrectBpmnErrors, bpmnErrorCorrectionPrompt);
    }

    private BpmnGenerationPromptGenerator() {
        super();
    }
}
