package org.rj.modelgen.llm.models.generation.options;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.prompt.PromptGeneratorCustomization;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.util.CloneableObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GenerationModelOptionsImpl<T extends GenerationModelOptionsImpl<T>> implements CloneableObject {
    private PromptGeneratorCustomization<PromptGenerator<?>> promptGeneratorCustomization;
    private Map<String, List<OverriddenLlmResponse>> overriddenLlmResponses = new HashMap<>();

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

    public void addOverriddenLlmResponse(String stateId, OverriddenLlmResponse override) {
        if (stateId == null || override == null) return;
        overriddenLlmResponses
                .computeIfAbsent(stateId, __ -> new ArrayList<>())
                .add(override);
    }

    public void addOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status) {
        addOverriddenLlmResponse(stateId, new OverriddenLlmResponse(overriddenResponse, status));
    }

    public void addOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status, Predicate<ModelInterfacePayload> condition) {
        addOverriddenLlmResponse(stateId, new OverriddenLlmResponse(overriddenResponse, status, condition));
    }

    public void addOverriddenLlmResponses(String stateId, List<OverriddenLlmResponse> overrides) {
        if (stateId == null || overrides == null) return;
        overrides.forEach(override -> addOverriddenLlmResponse(stateId, override));
    }

    public GenerationModelOptionsImpl<T> withOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status) {
        addOverriddenLlmResponse(stateId, overriddenResponse, status);
        return this;
    }

    public GenerationModelOptionsImpl<T> withOverriddenLlmResponse(String stateId, String overriddenResponse, ModelResponse.Status status, Predicate<ModelInterfacePayload> condition) {
        addOverriddenLlmResponse(stateId, overriddenResponse, status, condition);
        return this;
    }

    public<E extends Enum<E>> GenerationModelOptionsImpl<T> withOverriddenLlmResponse(E stateId, String overriddenResponse, ModelResponse.Status status) {
        return withOverriddenLlmResponse(stateId.toString(), overriddenResponse, status);
    }

    public<E extends Enum<E>> GenerationModelOptionsImpl<T> withOverriddenLlmResponse(E stateId, String overriddenResponse, ModelResponse.Status status, Predicate<ModelInterfacePayload> condition) {
        return withOverriddenLlmResponse(stateId.toString(), overriddenResponse, status, condition);
    }

    public Optional<List<OverriddenLlmResponse>> getOverriddenLlmResponsesIfPresent(String stateId) {
        return Optional.ofNullable(overriddenLlmResponses.getOrDefault(stateId, null));
    }

    public List<OverriddenLlmResponse> getOverriddenLlmResponses(String stateId) {
        return getOverriddenLlmResponsesIfPresent(stateId).orElseGet(ArrayList::new);
    }

    public boolean hasOverriddenLlmResponse(String stateId) {
        return getOverriddenLlmResponsesIfPresent(stateId)
                .map(responses -> !responses.isEmpty())
                .orElse(false);
    }

    @Override
    public GenerationModelOptionsImpl<T> clone() throws CloneNotSupportedException {
        final var cloned = (GenerationModelOptionsImpl<T>) super.clone();

        // Deep copy
        cloned.overriddenLlmResponses = this.overriddenLlmResponses.entrySet().stream()
                .map(e -> ImmutablePair.of(e.getKey(), new ArrayList<>(e.getValue())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a, b) -> b, HashMap::new));

        return cloned;
    }

}
