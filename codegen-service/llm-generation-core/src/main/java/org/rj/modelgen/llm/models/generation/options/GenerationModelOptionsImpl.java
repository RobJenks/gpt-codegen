package org.rj.modelgen.llm.models.generation.options;

import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.prompt.PromptGeneratorCustomization;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.util.CloneableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GenerationModelOptionsImpl<T extends GenerationModelOptionsImpl<T>> implements CloneableObject {
    private PromptGeneratorCustomization<PromptGenerator<?>> promptGeneratorCustomization;
    private Map<String, OverriddenLlmResponse> overriddenLlmResponses = new HashMap<>();

    // Prompt generator customization

    public void addPromptGeneratorCustomization(PromptGeneratorCustomization<PromptGenerator<?>> promptGeneratorCustomization) {
        this.promptGeneratorCustomization = promptGeneratorCustomization;
    }

    public GenerationModelOptionsImpl<T> withPromptGeneratorCustomization(PromptGeneratorCustomization<PromptGenerator<?>> promptGeneratorCustomization) {
        addPromptGeneratorCustomization(promptGeneratorCustomization);
        return this;
    }

    public boolean hasPromptGeneratorCustomization() {
        return promptGeneratorCustomization != null;
    }

    public <TPromptGenerator extends PromptGenerator<?>> TPromptGenerator applyPromptGeneratorCustomization(TPromptGenerator promptGenerator) {
        if (hasPromptGeneratorCustomization()) {
            promptGeneratorCustomization.accept(promptGenerator);
        }

        return promptGenerator;
    }


    // Overridden LLM responses

    public void addOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status) {
        if (stateId == null) return;
        overriddenLlmResponses.put(stateId, new OverriddenLlmResponse(overriddenResponse, status));
    }

    public GenerationModelOptionsImpl<T> withOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status) {
        addOverriddenLlmResponse(stateId, overriddenResponse, status);
        return this;
    }

    public<E extends Enum<E>> GenerationModelOptionsImpl<T> withOverriddenLlmResponse(E stateId, String overriddenResponse, ModelResponse.Status status) {
        return withOverriddenLlmResponse(stateId.toString(), overriddenResponse, status);
    }

    public Optional<OverriddenLlmResponse> getOverriddenLlmResponseIfPresent(String stateId) {
        return Optional.ofNullable(overriddenLlmResponses.getOrDefault(stateId, null));
    }

    public OverriddenLlmResponse getOverriddenLlmResponse(String stateId) {
        return getOverriddenLlmResponseIfPresent(stateId).orElse(null);
    }

    public boolean hasOverriddenLlmResponse(String stateId) {
        return getOverriddenLlmResponseIfPresent(stateId).isPresent();
    }

    @Override
    public GenerationModelOptionsImpl<T> clone() throws CloneNotSupportedException {
        final var cloned = (GenerationModelOptionsImpl<T>) super.clone();

        // Deep copy
        cloned.overriddenLlmResponses = new HashMap<>(this.overriddenLlmResponses);

        return cloned;
    }

    public static class OverriddenLlmResponse {
        private final String response;
        private final ModelResponse.Status status;

        public OverriddenLlmResponse(String response, ModelResponse.Status status) {
            this.response = response;
            this.status = status;
        }

        public String getResponse() {
            return response;
        }

        public ModelResponse.Status getStatus() {
            return status;
        }
    }
}
