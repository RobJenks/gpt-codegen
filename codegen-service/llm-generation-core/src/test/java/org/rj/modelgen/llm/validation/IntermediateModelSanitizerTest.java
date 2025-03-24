package org.rj.modelgen.llm.validation;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.validation.impl.GenericModelResponseSanitizer;
import org.rj.modelgen.llm.validation.impl.NoOpIntermediateModelSanitizer;

import java.util.HashMap;
import java.util.Map;
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

    @Test
    public void testGenericResponseSanitization_withInnerContentExtraction() throws Exception {
        final var input = """
                This is the content:
                ```
                start-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
                ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sfdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fsafdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                fdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz-end
                ```
                and that's it
                """;

        final var sanitized = sanitizeGeneric(input).strip();

        Assertions.assertTrue(sanitized.startsWith("start"));
        Assertions.assertTrue(sanitized.endsWith("end"));

        Assertions.assertFalse(sanitized.contains("This is the content"));
        Assertions.assertFalse(sanitized.contains("and that's it"));
    };

    @Test
    public void testGenericResponseSanitization_withContentBelowThreshold() throws Exception {
        final var input = """
                start-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
                ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
                Here is some code to illustrate.  We won't want to extract this as the
                inner content since it doesn't represent the full response:
                ```
                some inner code block
                some inner code block
                some inner code block
                ```
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sfdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf-end
                """;

        final var sanitized = sanitizeGeneric(input).strip();

        Assertions.assertTrue(sanitized.startsWith("start"));
        Assertions.assertTrue(sanitized.endsWith("end"));
    };

    @Test
    public void testGenericResponseSanitization_withExclusionOfMultipleBlocks() throws Exception {
        final var input = """
                start
                ```
                This is the first code block
                ```
                bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
                ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                dfasdfasffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                sfdasdfasdfasdffdasdfasdfasdffdasdfasdfasdffdasdfasdfasdf
                ```
                and this is the second code block.  The test validates that opening
                backticks of block 1 and closing backticks of block 2 are not
                treated as a response-spanning block that should be extracted
                ```
                end
                """;

        final var sanitized = sanitizeGeneric(input).strip();

        Assertions.assertTrue(sanitized.startsWith("start"));
        Assertions.assertTrue(sanitized.endsWith("end"));
    };

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

    private String sanitizeGeneric(String content) {
        return new GenericModelResponseSanitizer().sanitize(content);
    }
}
