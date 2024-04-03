package org.rj.modelgen.llm.prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PromptGenerator<TImpl extends PromptGenerator<?, TSelector>, TSelector> {
    private final Map<TSelector, String> prompts;

    public PromptGenerator() {
        this(Map.of());
    }

    public PromptGenerator(Map<TSelector, String> prompts) {
        this.prompts = new HashMap<>(prompts);
    }

    public Map<TSelector, String> getPrompts() {
        return this.prompts;
    }

    @SuppressWarnings("unchecked")
    public TImpl withAvailablePrompt(TSelector selector, String prompt) {
        this.prompts.put(selector, prompt);
        return (TImpl)this;
    }

    public Optional<String> getPrompt(TSelector selector) {
        return getPrompt(selector, List.of());
    }

    public Optional<String> getPrompt(TSelector selector, List<PromptSubstitution> substitutions) {
        return Optional.ofNullable(selector)
                .map(prompts::get)
                .map(raw -> substitutions.stream().reduce(raw,
                        (prompt, subst) -> subst.apply(prompt),     // Apply each substitution in turn
                        (a, b) -> b));
    }
}
