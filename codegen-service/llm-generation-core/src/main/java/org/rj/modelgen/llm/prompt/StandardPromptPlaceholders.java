package org.rj.modelgen.llm.prompt;

public interface StandardPromptPlaceholders {
    PromptPlaceholder SCHEMA_CONTENT = new PromptPlaceholder("SCHEMA_CONTENT");
    PromptPlaceholder CURRENT_STATE = new PromptPlaceholder("CURRENT_STATE");
    PromptPlaceholder PROMPT = new PromptPlaceholder("PROMPT");
    PromptPlaceholder COMPONENT_LIBRARY = new PromptPlaceholder("COMPONENT_LIBRARY");
    PromptPlaceholder RETURN_TO_HIGH_LEVEL = new PromptPlaceholder("RETURN_TO_HIGH_LEVEL");
}
