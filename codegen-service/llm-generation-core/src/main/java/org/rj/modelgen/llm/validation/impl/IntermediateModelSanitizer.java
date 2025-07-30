package org.rj.modelgen.llm.validation.impl;

import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.util.Util;
import org.rj.modelgen.llm.validation.ResponseSanitizer;

import java.util.Optional;
import java.util.regex.Pattern;

public abstract class IntermediateModelSanitizer<TModel extends IntermediateModel> extends ResponseSanitizer {
    private static final Pattern JSON_EXTRACT = Pattern.compile("^.*?(\\{.*}).*?$", Pattern.DOTALL | Pattern.MULTILINE);
    private static final String FIX_INVALID_ESCAPES = "([^\\\\])\\\\([^\"\\\\/bfnrt])";

    private final Class<TModel> modelClass;

    public IntermediateModelSanitizer(Class<TModel> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * Sanitize a model IR response by applying the following sequence of operations in turn
     *
     * @param content   Input content to be sanitized
     * @return          Sanitized output
     */
    public String sanitize(String content) {
        return Optional.ofNullable(content)
                .map(this::removeInvalidControlData)
                .map(this::extractJsonIfRequired)
                .map(this::fixInvalidEscapeCharacters)
                .map(this::performCustomSanitization)
                .map(this::parseModelStructure)
                .map(this::performCustomModelSanitization)
                .map(this::serializeModel)
                .orElse(content);
    }

    /**
     * Entry point for subclasses to insert their own sanitization logic.  Performed after custom sanitization
     * is run on the raw content.  This method operates on the intermediate model itself
     *
     * @param model     Input model to be sanitized
     * @return          Sanitized output model
     */
    protected abstract TModel performCustomModelSanitization(TModel model);

    /**
     * Some LLM services can return invalid data within the payload, for example control chars, which are invalid within
     * JSON and other serialized formats.  Remove any non-printable control char (< ASC 32) to avoid any chance of issues
     */
    private String removeInvalidControlData(String content) {
        return content.replaceAll("\\p{Cntrl}", "");
    }

    /**
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


    /**
     * Fix escape characters: some models / model providers may not correctly escape JSON when returned as part of
     * a larger free-text response.  Identify and fix any escapes which are not valid in the JSON spec
     */
    private String fixInvalidEscapeCharacters(String content) {
        return content.replaceAll(FIX_INVALID_ESCAPES, "$1$2");
    }

    /**
     * Attempt to parse serialized content into the expected model structure
     * @param content       Raw content to be parsed into the model structure
     * @return              Intermediate model
     */
    private TModel parseModelStructure(String content) {
        return Util.deserializeOrThrow(content, modelClass, e -> new LlmGenerationModelException(
                String.format("Failed to parse intermediate model content into expected model structure: %s", e.getMessage()), e));
    }

    /**
     * Attempt to serialize the model back into raw string content.  Performed after all sanitization is complete
     * @param model         Intermediate model
     * @return              Serialized model content
     */
    private String serializeModel(TModel model) {
        return Util.serializeOrThrow(model, e -> new LlmGenerationModelException(
                String.format("Failed to serialize intermediate model after sanitization: %s", e.getMessage()), e));
    }

}
