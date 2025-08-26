package org.rj.modelgen.bpmn.models.generation.base.context;

import org.rj.modelgen.llm.prompt.PromptPlaceholder;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;

public interface BpmnPromptPlaceholders extends StandardPromptPlaceholders { // TODO: ? are there any prompt placeholders for BPMNs
    PromptPlaceholder COMPONENT_LIBRARY = new PromptPlaceholder("COMPONENT_LIBRARY");

    PromptPlaceholder HIGH_LEVEL_MODEL_SUMMARY = new PromptPlaceholder("HIGH_LEVEL_MODEL_SUMMARY");

    // Set of validation fixes to apply to detail-level generation on retry of model generation
    PromptPlaceholder DETAIL_MODEL_VALIDATION_ISSUES = new PromptPlaceholder("DETAIL_MODEL_VALIDATION_ISSUES");
}

