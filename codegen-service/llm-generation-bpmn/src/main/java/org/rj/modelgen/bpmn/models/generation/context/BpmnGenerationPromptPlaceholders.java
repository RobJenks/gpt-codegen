package org.rj.modelgen.bpmn.models.generation.context;

import org.rj.modelgen.llm.prompt.PromptPlaceholder;

public class BpmnGenerationPromptPlaceholders {
    public static final PromptPlaceholder SCHEMA_CONTENT = new PromptPlaceholder("SCHEMA_CONTENT");
    public static final PromptPlaceholder CURRENT_STATE = new PromptPlaceholder("CURRENT_STATE");
    public static final PromptPlaceholder PROMPT = new PromptPlaceholder("PROMPT");
}
