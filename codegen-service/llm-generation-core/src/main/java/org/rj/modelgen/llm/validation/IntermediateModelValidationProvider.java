package org.rj.modelgen.llm.validation;

import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationErrors;

import java.util.List;
import java.util.stream.Collectors;

public class IntermediateModelValidationProvider<TModel extends IntermediateModel> {
    private final ModelSchema modelSchema;
    private final IntermediateModelSanitizer sanitizer;
    private final IntermediateModelParser<TModel> parser;

    public IntermediateModelValidationProvider(ModelSchema modelSchema, Class<? extends TModel> modelClass) {
        this.modelSchema = modelSchema;
        this.parser = new IntermediateModelParser<>(modelClass);
        this.sanitizer = new IntermediateModelSanitizer();
    }

    public IntermediateModelValidationErrors validate(String content) {
        if (StringUtils.isBlank(content)) return IntermediateModelValidationErrors.singleMessage("Empty prompt");

        final var sanitizedContent = sanitizer.sanitize(content);

        // Validate the node data complies with the schema and all its requirements
        final var schemaErrors = validateSchemaAndReturnErrors(modelSchema.getSchemaContent(), sanitizedContent);
        if (!schemaErrors.isEmpty()) {
            return new IntermediateModelValidationErrors(schemaErrors);
        }

        // Make sure the response can be deserialized into the intermediate structure (should ~always be true now if we pass the schema validation)
        final var parseResult = parser.parse(sanitizedContent);
        if (parseResult.isErr()) {
            return IntermediateModelValidationErrors.singleMessage(String.format(
                    "Response does not conform to required schema (%s)", parseResult.getError()));
        }

        // Passed all checks
        return IntermediateModelValidationErrors.empty();
    }

    public static List<IntermediateModelValidationError> validateSchemaAndReturnErrors(String schemaContent, String data) {
        try {
            JSONObject rawSchema = new JSONObject(schemaContent);
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(new JSONObject(data)); // Throws a ValidationException if this object is invalid

            return List.of();
        }
        catch (ValidationException ex) {
            return ex.getAllMessages().stream()
                    .map(err -> new IntermediateModelValidationError(
                            String.format("Does not comply with JSON schema (%s)", err)))
                    .collect(Collectors.toList());
        }
    }
}
