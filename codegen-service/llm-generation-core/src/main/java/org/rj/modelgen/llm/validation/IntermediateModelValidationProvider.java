package org.rj.modelgen.llm.validation;

import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
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
    private final IntermediateModelParser<TModel> parser;

    public IntermediateModelValidationProvider(ModelSchema modelSchema, Class<? extends TModel> modelClass) {
        this.modelSchema = modelSchema;
        this.parser = new IntermediateModelParser<>(modelClass);
    }

    public IntermediateModelValidationErrors validate(String content) {
        if (StringUtils.isBlank(content)) return IntermediateModelValidationErrors.singleMessage("Empty prompt");

        // Validate the node data complies with the schema and all its requirements
        final var schemaErrors = validateSchemaAndReturnErrors(modelSchema.getSchemaContent(), content);
        if (!schemaErrors.isEmpty()) {
            return new IntermediateModelValidationErrors()
                    .withErrors(schemaErrors)
                    .withError(reportContent(content));
        }

        // Make sure the response can be deserialized into the intermediate structure (should ~always be true now if we pass the schema validation)
        final var parseResult = parser.parse(content);
        if (parseResult.isErr()) {
            return new IntermediateModelValidationErrors()
                    .withError(new IntermediateModelValidationError(
                            String.format("Response does not conform to required schema (%s)", parseResult.getError())))
                    .withError(reportContent(content));
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
        catch (JSONException ex) {
            return List.of(new IntermediateModelValidationError(String.format(
                    "Intermediate model data is not valid JSON (%s).  Data is: '%s'", ex.getMessage(), data)));
        }
        catch (ValidationException ex) {
            return ex.getAllMessages().stream()
                    .map(err -> new IntermediateModelValidationError(
                            String.format("Does not comply with JSON schema (%s)", err)))
                    .collect(Collectors.toList());
        }
    }

    private static IntermediateModelValidationError reportContent(String content) {
        if (content == null) return null;
        return new IntermediateModelValidationError("Content with errors: " + content);
    }
}
