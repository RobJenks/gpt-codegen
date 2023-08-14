package org.rj.modelgen.llm.integrations.openai;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.beans.ContextEntry;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.response.ModelResponseTransformer;
import org.rj.modelgen.llm.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenAIModelResponseTransformer implements ModelResponseTransformer<OpenAIModelResponse> {
    @Override
    public ModelResponse transform(OpenAIModelResponse response) {
        final var modelResponse = new ModelResponse();
        modelResponse.setStatus(ModelResponse.Status.SUCCESS);  // Will currently return errors before this point if != success
        modelResponse.setMessage(Optional.ofNullable(response.getChoices()).orElseGet(List::of).stream()
                        .map(OpenAIModelResponse.Choice::getMessage)
                        .map(ContextEntry::getContent)
                        .filter(msg -> !StringUtils.isBlank(msg))
                        .findFirst()
                        .orElse(null));

        // OpenAI responses include metadata on token usage, model choices and the request itself.  Attach all of this
        // metadata to the response for now
        final var metadata = Util.convertOrThrow(response, Map.class, ex -> new RuntimeException(
                String.format("Could not collect metadata from Open AI response (%s)", ex.getMessage()), ex));
        modelResponse.setMetadata(metadata);

        return modelResponse;
    }
}
