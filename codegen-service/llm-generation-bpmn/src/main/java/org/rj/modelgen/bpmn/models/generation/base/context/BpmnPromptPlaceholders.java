package org.rj.modelgen.bpmn.models.generation.base.context;

import org.rj.modelgen.llm.prompt.PromptPlaceholder;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;

public interface BpmnPromptPlaceholders extends StandardPromptPlaceholders {
    PromptPlaceholder COMPONENT_LIBRARY = new PromptPlaceholder("COMPONENT_LIBRARY");
    PromptPlaceholder GLOBAL_VARIABLE_LIBRARY = new PromptPlaceholder("GLOBAL_VARIABLE_LIBRARY");

    PromptPlaceholder HIGH_LEVEL_MODEL_SUMMARY = new PromptPlaceholder("HIGH_LEVEL_MODEL_SUMMARY");

    // Set of global variables filtered to only those detected in the initial prompt
    PromptPlaceholder PROMPT_RELEVANT_GLOBAL_VARIABLES = new PromptPlaceholder("PROMPT_RELEVANT_GLOBAL_VARIABLES");

    // Set of global variables filtered to only those used in the generated high-level model
    PromptPlaceholder GLOBAL_VARIABLES_USED_IN_HL_MODEL = new PromptPlaceholder("GLOBAL_VARIABLES_USED_IN_HL_MODEL");

    // Set of validation fixes to apply to detail-level generation on retry of model generation
    PromptPlaceholder DETAIL_MODEL_VALIDATION_ISSUES = new PromptPlaceholder("DETAIL_MODEL_VALIDATION_ISSUES");
}

