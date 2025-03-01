package org.rj.modelgen.bpmn.models.generation.base.context;

import org.rj.modelgen.llm.util.StringSerializable;

public enum BpmnGenerationPromptType implements StringSerializable {
    Generate,
    GenerationInitialPrompt,
    CorrectSchemaErrors,
    CorrectBpmnErrors;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
