package org.rj.modelgen.llm.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.validation.ResponseSanitizer;

import java.util.Optional;
import java.util.regex.Pattern;

public class GenericModelResponseSanitizer extends ResponseSanitizer {
    // Block delimiter in standard model responses
    private static final String BLOCK_DELIMITER = "```";
    private static final Pattern BLOCK_DELIMITER_PATTERN = Pattern.compile(String.format(
            "%s([\\s\\S]*)%s", BLOCK_DELIMITER, BLOCK_DELIMITER));

    // An outer ```block``` containing >= this percentage of the response content will be treated
    // as the full model response, and extracted during sanitization
    private static final float FULL_RESPONSE_BLOCK_PERCENTAGE_THRESHOLD = 0.95f;

    public GenericModelResponseSanitizer() { }

    /**
     * Sanitize the given generic (likely natural language) model response
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized response content
     */
    @Override
    public String sanitize(String content) {
        return Optional.ofNullable(content)
                .map(this::extractContentIfRequired)
                .map(this::performCustomSanitization)
                .orElse(content);
    }

    // No custom sanitization required at the base response level.  Can be implemented by subclasses as required
    @Override
    protected String performCustomSanitization(String content) {
        return content;
    }

    /**
     * Extract content from within outer ```blocks``` if required.  Run heuristically based on percentage of
     * response which this block represents.  We assume that a sufficiently-large block is the full response
     * This catches the issue where a model will include 1-2 lines confirmation message before the content itself
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized response content
     */
    private String extractContentIfRequired(String content) {
        if (StringUtils.isBlank(content)) return content;

        final var start = content.indexOf(BLOCK_DELIMITER);
        final var end = content.lastIndexOf(BLOCK_DELIMITER);

        if (start >= 0 && end >= 0 && start < end) {
            final var innerContent = content.substring(start + BLOCK_DELIMITER.length(), end);

            // Only match this block if it does not contain any block delimiters itself.  Otherwise this could just
            // be several independent code blocks where the start of block 0 and end of block N appear to be a
            // full response-spanning block
            if (!innerContent.contains(BLOCK_DELIMITER)) {

                // Only consider this the full response content if it is >= threshold
                final float percentageOfContent = ((float) innerContent.length() / (float) content.length());
                if (percentageOfContent >= FULL_RESPONSE_BLOCK_PERCENTAGE_THRESHOLD) {
                    return innerContent.strip();
                }
            }
        }

        return content;
    }
}
