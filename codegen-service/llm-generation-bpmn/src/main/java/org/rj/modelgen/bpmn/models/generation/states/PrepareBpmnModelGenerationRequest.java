package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.llm.context.provider.impl.ConstrainedBpmnGenerationContextProvider;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.context.BpmnPromptPlaceholders;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptType;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.PrepareModelGenerationRequest;

import java.util.List;


public class PrepareBpmnModelGenerationRequest extends PrepareModelGenerationRequest {
    private final BpmnGenerationPromptGenerator promptGenerator;

    public PrepareBpmnModelGenerationRequest(ModelSchema modelSchema, BpmnGenerationPromptGenerator promptGenerator) {
        super(PrepareBpmnModelGenerationRequest.class, modelSchema, new ConstrainedBpmnGenerationContextProvider(promptGenerator));
        this.promptGenerator = promptGenerator;
    }

    @Override
    public String getDescription() {
        return "Prepare BPMN model generation request";
    }

    @Override
    protected String buildGenerationPrompt(ModelSchema modelSchema, Context context, String request) {
        return promptGenerator.getPrompt(BpmnGenerationPromptType.Generate, List.of(
                new PromptSubstitution(BpmnPromptPlaceholders.SCHEMA_CONTENT, modelSchema.getSchemaContent()),
                new PromptSubstitution(BpmnPromptPlaceholders.CURRENT_STATE, context.getLatestModelEntry()
                        .orElseGet(() -> ContextEntry.forModel("{}")).getContent()),
                new PromptSubstitution(BpmnPromptPlaceholders.PROMPT, request)))

                .orElseThrow(() -> new BpmnGenerationException("Could not generate new BPMN generation prompt"));
    }

    @Override
    public String getSuccessSignalId() {
        return BpmnGenerationSignals.SubmitRequestToLlm.toString();
    }
}
