package org.rj.modelgen.llm.prompt;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.util.Util;

import java.util.Optional;

public class PromptSubstitution {
    private final String existingString;
    private final String newString;

    public String getExistingString() {
        return existingString;
    }

    public String getNewString() {
        return newString;
    }

    public PromptSubstitution(String existingString, String newString) {
        this.existingString = existingString;
        this.newString = newString;
    }

    public PromptSubstitution(PromptPlaceholder existingPlaceholder, String newString) {
        this.existingString = existingPlaceholder.getValue();
        this.newString = newString;
    }



    public String apply(String targetString) {
        if (existingString == null || newString == null) return targetString;

        return StringUtils.replace(targetString, existingString, newString);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", componentToString(existingString), componentToString(newString));
    }

    private String componentToString(String comp) {
        if (comp == null) {
            return "<null>";
        }

        return String.format("\"%s\"", Util.displayStringWithMaxLength(comp, 50));
    }
}
