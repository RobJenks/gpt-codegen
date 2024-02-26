package org.rj.modelgen.llm.schema;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import java.util.List;

public class

ModelSchema {
    private final String schemaContent;
    private final Schema schema;

    public ModelSchema(String schemaContent) {
        this.schemaContent = schemaContent;

        final JSONObject rawSchema = new JSONObject(schemaContent);
        this.schema = SchemaLoader.load(rawSchema);
    }

    public ValidationResult validate(String data) {
        try {
            schema.validate(new JSONObject(data)); // Throws a ValidationException if this object is invalid

            return new ValidationResult(true, List.of());
        }
        catch (ValidationException ex) {
            return new ValidationResult(false, ex.getAllMessages());
        }
    }

    public String getSchemaContent() {
        return schemaContent;
    }


    public static class ValidationResult {
        private final boolean valid;
        private List<String> validationErrors;

        public ValidationResult(boolean valid, List<String> validationErrors) {
            this.valid = valid;
            this.validationErrors = validationErrors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getValidationErrors() {
            return validationErrors;
        }
    }

}
