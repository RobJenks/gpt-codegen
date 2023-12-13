package org.rj.modelgen.llm.prompt;

import org.apache.commons.lang3.StringUtils;

public class PromptSubstitution {
    private final String existingString;
    private final String newString;

    public PromptSubstitution(String existingString, String newString) {
        this.existingString = existingString;
        this.newString = newString;
    }

    public PromptSubstitution(PromptPlaceholder existingPlaceholder, String newString) {
        this.existingString = existingPlaceholder.get();
        this.newString = newString;
    }



    public String apply(String targetString) {
        if (existingString == null || newString == null) return targetString;

        return StringUtils.replace(targetString, existingString, newString);
    }
}
