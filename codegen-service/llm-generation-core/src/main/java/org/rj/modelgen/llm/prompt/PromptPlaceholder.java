package org.rj.modelgen.llm.prompt;

public class PromptPlaceholder {
    private final String value;

    public PromptPlaceholder(String value) {
        this.value = value;
    }

    public String get() {
        return "${" + value + "}";
    }

    @Override
    public String toString() {
        return get();
    }
}
