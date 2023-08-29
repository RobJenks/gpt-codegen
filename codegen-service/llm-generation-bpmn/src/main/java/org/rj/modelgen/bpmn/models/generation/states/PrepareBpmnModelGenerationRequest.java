package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptGenerator;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptPlaceholders;
import org.rj.modelgen.bpmn.models.generation.context.BpmnGenerationPromptType;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;

public class PrepareBpmnModelGenerationRequest extends ModelInterfaceState<NewBpmnGenerationRequestReceived> {
    private final ModelSchema modelSchema;
    private final BpmnGenerationPromptGenerator promptGenerator;

    public PrepareBpmnModelGenerationRequest(ModelSchema modelSchema, BpmnGenerationPromptGenerator promptGenerator) {
        super(PrepareBpmnModelGenerationRequest.class);
        this.modelSchema = modelSchema;
        this.promptGenerator = promptGenerator;
    }

    @Override
    public String getDescription() {
        return "Prepare BPMN model generation request";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        final var prompt = promptGenerator.getPrompt(BpmnGenerationPromptType.Generate, List.of(
                new PromptSubstitution(BpmnGenerationPromptPlaceholders.SCHEMA_CONTENT, modelSchema.getSchemaContent()),
                new PromptSubstitution(BpmnGenerationPromptPlaceholders.PROMPT, input.getRequest())
        ));

        // TODO: 1
        return Mono.just(new LlmModelRequestPreparedSuccessfully(input.getCurrentIL() + "," + input.getRequest()));
    }
}
