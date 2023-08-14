package org.rj.modelgen.llm.integrations.openai;

import org.rj.modelgen.llm.beans.ContextEntry;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenAIModelRequestTransformer implements ModelRequestTransformer<OpenAIModelRequest> {
    @Override
    public OpenAIModelRequest transform(ModelRequest request) {
        final var openAiRequest = new OpenAIModelRequest();
        openAiRequest.setModel(request.getModel());
        openAiRequest.setTemperature(request.getTemperature());

        openAiRequest.setMessages(Optional.ofNullable(request.getMessages()).orElseGet(List::of).stream()
                .map(msg -> new ContextEntry(transformRole(msg.getRole()), msg.getMessage()))
                .collect(Collectors.toList()));

        return openAiRequest;
    }

    private String transformRole(ModelRequest.Message.Role role) {
        // Transform to a role type expected by the OpenAI API
        return switch (role) {
            case USER  -> OpenAIConstants.ROLE_USER;
            default    -> OpenAIConstants.ROLE_ASSISTANT;
        };
    }
}
