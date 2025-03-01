package org.rj.modelgen.llm.prompt;

import org.rj.modelgen.llm.util.StringSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PromptGenerator<TImpl extends PromptGenerator<?>> {
    private final Map<String, String> prompts;

    public PromptGenerator() {
        this(Map.of());
    }

    public PromptGenerator(Map<String, String> prompts) {
        this.prompts = new HashMap<>(prompts);
    }

    protected Map<String, String> getPrompts() {
        return this.prompts;
    }

    public TImpl withAvailablePrompt(StringSerializable selector, String prompt) {
        return withAvailablePrompt(selector.toString(), prompt);
    }

    @SuppressWarnings("unchecked")
    public TImpl withAvailablePrompt(String selector, String prompt) {
        addPrompt(selector, prompt);
        return (TImpl)this;
    }

    public void addPrompt(StringSerializable selector, String prompt) {
        addPrompt(selector.toString(), prompt);
    }

    public void addPrompt(String selector, String prompt) {
        this.prompts.put(selector, prompt);
    }

    public Optional<String> getPrompt(StringSerializable selector) {
        return getPrompt(selector, List.of());
    }

    public Optional<String> getPrompt(StringSerializable selector, List<PromptSubstitution> substitutions) {
        return getPrompt(selector.toString(), substitutions);
    }

    public Optional<String> getPrompt(String selector, List<PromptSubstitution> substitutions) {
        return Optional.ofNullable(selector)
                .map(prompts::get)
                .map(raw -> substitutions.stream().reduce(raw,
                        (prompt, subst) -> subst.apply(prompt),     // Apply each substitution in turn
                        (a, b) -> b));
    }
}
