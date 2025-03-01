package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.List;
import java.util.Optional;

public class PrepareGenericModelRequest<TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>> extends PrepareModelGenerationRequest {
    private final TPromptGenerator promptGenerator;
    private final StringSerializable promptType;

    public PrepareGenericModelRequest(ContextProvider contextProvider, TPromptGenerator promptGenerator, StringSerializable promptType) {
        super(PrepareGenericModelRequest.class, null, contextProvider);
        this.promptGenerator = promptGenerator;
        this.promptType = promptType;
    }

    @Override
    protected Optional<String> buildGenerationPrompt(ModelSchema modelSchema, Context context, String request, List<PromptSubstitution> substitutions) {
        return promptGenerator.getPrompt(promptType, substitutions);
    }
}
