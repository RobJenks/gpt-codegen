package org.rj.modelgen.llm.integrations.openai;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestTransformer;
import org.rj.modelgen.llm.context.ContextRole;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenAIModelRequestTransformer implements ModelRequestTransformer<OpenAIModelRequest> {
    @Override
    public OpenAIModelRequest transform(ModelRequest request) {
        final var openAiRequest = new OpenAIModelRequest();
        openAiRequest.setModel(request.getModel());
        openAiRequest.setTemperature(request.getTemperature());

        openAiRequest.setMessages(Optional.ofNullable(request.getContext())
                .map(Context::getData)
                .orElseGet(List::of)
                .stream()

                .map(this::transformContextEntry)
                .collect(Collectors.toList()));

        return openAiRequest;
    }

    private OpenAIContextMessage transformContextEntry(ContextEntry contextEntry) {
        return new OpenAIContextMessage(transformRole(contextEntry.getRole()), contextEntry.getContent());
    }

    private String transformRole(ContextRole role) {
        // Transform to a role type expected by the OpenAI API
        return switch (role) {
            case USER  -> OpenAIConstants.ROLE_USER;
            default    -> OpenAIConstants.ROLE_ASSISTANT;
        };
    }
}
