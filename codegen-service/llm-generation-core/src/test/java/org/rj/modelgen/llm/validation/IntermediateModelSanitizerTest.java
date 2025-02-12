package org.rj.modelgen.llm.validation;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.validation.generic.NoOpIntermediateModelSanitizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class IntermediateModelSanitizerTest {

    @Test
    public void testSimpleJsonExtraction() throws Exception {
        final var input = "This is a test containing { \"nodes\": [{ \"id\": \"some-json\" }] } which should be extracted";
        testJsonSanitization(input, "{ \"nodes\": [{ \"id\": \"some-json\" }] }");
    }

    @Test
    public void testHandlingOfInvalidEscapes() throws Exception {
        final var input =
                """
                    {
                      "nodes": [
                        {
                          "id": "some-id",
                          "ref": "\\#/\\$defs/element",
                          "other": "allowed\\\\backslash"
                        }
                      ]
                    }
                """;

        testJsonSanitization(input,
                """
                    {
                      "nodes": [
                        {
                          "id": "some-id",
                          "ref": "#/$defs/element",
                          "other": "allowed\\\\backslash"
                        }
                      ]
                    }
                    """);
    }


    private void testJsonSanitization(String input, String expected) {
        final var sanitizer = new NoOpIntermediateModelSanitizer();
        final var sanitized = sanitizer.sanitize(input);

        final var expectedJson = withoutNulls(new JSONObject(expected));
        final var sanitizedJson = withoutNulls(new JSONObject(sanitized));

        Assertions.assertEquals(expectedJson.toMap(), sanitizedJson.toMap());   // JSONObject has no .equals()
    }

    private JSONObject withoutNulls(JSONObject json) {
        if (json == null) return null;
        final var data = json.toMap().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));

        return new JSONObject(data);
    }
}
