package org.rj.modelgen.llm.validation;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.validation.generic.GenericIntermediateModelSanitizer;

class IntermediateModelSanitizerTest {

    @Test
    public void testSimpleJsonExtraction() throws Exception {
        final var input = "This is a test containing { \"some\": \"json\" } which should be extracted";
        testJsonSanitization(input, "{ \"some\": \"json\" }");
    }

    @Test
    public void testHandlingOfInvalidEscapes() throws Exception {
        final var input =
                """
                    {
                        "\\$id": "some-id",
                        "\\$ref": "\\#/\\$defs/element",
                        "other": "allowed\\\\backslash"
                    }
                """;

        testJsonSanitization(input,
                """
                    {
                        "$id": "some-id",
                        "$ref": "#/$defs/element",
                        "other": "allowed\\\\backslash"
                    }
                    """);
    }


    private void testJsonSanitization(String input, String expected) {
        final var sanitizer = new GenericIntermediateModelSanitizer();
        final var sanitized = sanitizer.sanitize(input);

        final var expectedJson = new JSONObject(expected);
        final var sanitizedJson = new JSONObject(sanitized);

        Assertions.assertEquals(expectedJson.toMap(), sanitizedJson.toMap());   // JSONObject has no .equals()
    }

}