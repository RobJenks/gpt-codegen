package org.rj.modelgen.llm.validation;

import java.util.Optional;
import java.util.regex.Pattern;

public class IntermediateModelSanitizer {
    private static final Pattern JSON_EXTRACT = Pattern.compile("^.*?(\\{.*}).*?$", Pattern.DOTALL | Pattern.MULTILINE);
    private static final String FIX_INVALID_ESCAPES = "([^\\\\])\\\\([^\"\\\\/bfnrt])";

    /**
     * Sanitize a model IR response by applying the following sequence of operations in turn
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized output
     */
    public String sanitize(String content) {
        return Optional.ofNullable(content)
                .map(this::extractJsonIfRequired)
                .map(this::fixInvalidEscapeCharacters)
                .orElse(content);
    }

    /***
     * Extract JSON: attempt to locate the largest JSON block within the output, in case of additional text
     * in violation of prompt constraints
     */
    private String extractJsonIfRequired(String content) {
        final var matcher = JSON_EXTRACT.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return content;
    }


    /***
     * Fix escape characters: some models / model providers may not correctly escape JSON when returned as part of
     * a larger free-text response.  Identify and fix any escapes which are not valid in the JSON spec
     */
    private String fixInvalidEscapeCharacters(String content) {
        return content.replaceAll(FIX_INVALID_ESCAPES, "$1$2");
    }

}
