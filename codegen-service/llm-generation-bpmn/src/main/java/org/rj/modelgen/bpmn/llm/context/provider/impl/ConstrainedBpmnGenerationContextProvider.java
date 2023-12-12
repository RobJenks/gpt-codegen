package org.rj.modelgen.bpmn.llm.context.provider.impl;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptType;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.impl.DefaultConstrainedContextProvider;

public class ConstrainedBpmnGenerationContextProvider extends DefaultConstrainedContextProvider {
    private final BpmnGenerationPromptGenerator promptGenerator;

    public ConstrainedBpmnGenerationContextProvider(BpmnGenerationPromptGenerator promptGenerator) {
        super();
        this.promptGenerator = promptGenerator;
    }

    @Override
    public Context newContext() {
        final var context = super.newContext();

        final var initialState = promptGenerator.getPrompt(BpmnGenerationPromptType.GenerationInitialPrompt)
                .orElseThrow(() -> new BpmnGenerationException("Cannot create initial BPMN generation context state"));

        context.addEntry(ContextEntry.forModel(initialState));
        return context;
    }
}
