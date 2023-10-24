package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.llm.context.provider.impl.ConstrainedBpmnGenerationContextProvider;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptPlaceholders;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptType;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class PrepareBpmnModelGenerationRequest extends ModelInterfaceState<NewBpmnGenerationRequestReceived> {
    private final ModelSchema modelSchema;
    private final BpmnGenerationPromptGenerator promptGenerator;
    private final ContextProvider contextProvider;

    public PrepareBpmnModelGenerationRequest(ModelSchema modelSchema, BpmnGenerationPromptGenerator promptGenerator) {
        super(PrepareBpmnModelGenerationRequest.class);
        this.modelSchema = modelSchema;
        this.promptGenerator = promptGenerator;

        this.contextProvider = new ConstrainedBpmnGenerationContextProvider(promptGenerator);
    }

    @Override
    public String getDescription() {
        return "Prepare BPMN model generation request";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        final var context = Optional.ofNullable(input.getCurrentContext())
                .orElseGet(contextProvider::newContext);

        final var prompt = promptGenerator.getPrompt(BpmnGenerationPromptType.Generate, List.of(
                new PromptSubstitution(BpmnGenerationPromptPlaceholders.SCHEMA_CONTENT, modelSchema.getSchemaContent()),
                new PromptSubstitution(BpmnGenerationPromptPlaceholders.PROMPT, input.getRequest())))
                .orElseThrow(() -> new BpmnGenerationException("Could not generate new prompt"));

        final var newContext = contextProvider.withPrompt(context, prompt);
        getModelInterface().getOrCreateSession(input.getSessionId()).replaceContext(newContext);

        return Mono.just(new LlmModelRequestPreparedSuccessfully(input.getSessionId(), newContext));
    }
}
