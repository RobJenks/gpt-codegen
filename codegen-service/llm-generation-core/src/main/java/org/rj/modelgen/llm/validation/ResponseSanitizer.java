package org.rj.modelgen.llm.validation;

public abstract class ResponseSanitizer {

    /**
     * Sanitize a model response by applying a type-specific sequence of operations in turn
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized output
     */
    public abstract String sanitize(String content);

    /**
     * Entry point for subclasses to insert their own sanitization logic.  Performed after all standard
     * operations are completed
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized output
     */
    protected abstract String performCustomSanitization(String content);

}
