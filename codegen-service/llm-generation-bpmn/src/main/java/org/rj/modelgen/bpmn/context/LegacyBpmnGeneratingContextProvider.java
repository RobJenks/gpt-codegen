package org.rj.modelgen.bpmn.context;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.rj.modelgen.llm.schema.model.ElementNode;
import org.rj.modelgen.llm.schema.model.IntermediateModel;
import org.rj.modelgen.bpmn.generation.BasicBpmnModelGenerator;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.LegacyContextProvider;
import org.rj.modelgen.llm.context.ContextRole;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.session.SessionState;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LegacyBpmnGeneratingContextProvider extends LegacyContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyBpmnGeneratingContextProvider.class);
    private static final String REJECT_TOKEN = "NO";
    private static final String PLACEHOLDER_SCHEMA = "${SCHEMA_CONTENT}";
    private static final String PLACEHOLDER_PROMPT = "${PROMPT}";
    private static final Pattern JSON_EXTRACT = Pattern.compile("^.*?(\\{.*}).*?$", Pattern.DOTALL | Pattern.MULTILINE);

    final BasicBpmnModelGenerator generator = new BasicBpmnModelGenerator();
    final String schema;
    final String promptTemplate;

    public LegacyBpmnGeneratingContextProvider() {
        schema = Util.loadStringResource("content/bpmn-intermediate-schema.json");
        promptTemplate = Util.loadStringResource("content/bpmn-prompt-template");
    }

    @Override
    public ModelRequest buildBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, this::constrainedPrompt);
    }

    @Override
    public ModelRequest buildUndecoratedBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, Function.identity());
    }

    public ModelRequest buildBodyWithDecoration(SessionState session, String prompt, Function<String, String> decorator) {
        final var context = session.getContext().hasLatestModelEntry() ?
                withContinuationContext(session, decorator.apply(prompt)) :
                withInitialContext(decorator.apply(prompt));

        LOG.info("Generated BPMN context: {}", Util.serializeOrThrow(context));

        final var body = new ModelRequest("gpt-4", 0.7f, new Context(context));
        return body;
    }



    private List<ContextEntry> withInitialContext(String prompt) {
        return List.of(
                ContextEntry.forModel(buildInitialNodeData().serialize()),
                ContextEntry.forUser(prompt)
        );
    }

    private IntermediateModel buildInitialNodeData() {
        final var nodeData = new IntermediateModel();

        final var startNode = new ElementNode("startProcess", "Start Process", "startEvent");
        startNode.setConnectedTo(List.of(new ElementNode.Connection("endProcess", "End the process immediately")));
        nodeData.getNodes().add(startNode);
        nodeData.getNodes().add(new ElementNode("endProcess", "End Process", "endEvent"));

        return nodeData;
    }

    private List<ContextEntry> withContinuationContext(SessionState session, String prompt) {
        String lastValidCode = null;
        for (int i = session.getContext().getData().size() - 1; i >= 0; --i) {
            final var event = session.getContext().getData().get(i);

            if (event.getRole() != ContextRole.MODEL) continue;
            if (event.getContent().equals(REJECT_TOKEN)) continue;

            lastValidCode = event.getContent();
            break;
        }

        if (lastValidCode == null) {
            return withInitialContext(prompt);
        }

        return List.of(
                ContextEntry.forModel(lastValidCode),
                ContextEntry.forUser(prompt)
        );
    }

    private String constrainedPrompt(String prompt) {
        return StringUtils.replaceEach(promptTemplate,
                new String[] { PLACEHOLDER_SCHEMA, PLACEHOLDER_PROMPT },
                new String[] { schema, prompt });
    }

    @Override
    public List<String> validateResponse(String response) {
        if (StringUtils.isBlank(response)) return List.of("Empty prompt");

        // Basic sanitizing; attempt to locate the largest JSON block within the output, in case of additional text
        // in violation of prompt constraints
        final var sanitized = sanitizeResponse(response);

        // Validate the node data complies with the schema and all its requirements
        final var schemaErrors = validateSchemaAndReturnErrors(schema, sanitized);
        if (!schemaErrors.isEmpty()) {
            return schemaErrors.stream().map(err -> String.format("Does not comply with JSON schema (%s)", err)).collect(Collectors.toList());
        }

        // Make sure the response can be deserialized into the intermediate structure (should ~always be true now if we pass the schema validation)
        IntermediateModel intermediateModel = null;
        try {
            intermediateModel = Util.getObjectMapper().readValue(sanitized, IntermediateModel.class);
        }
        catch (Exception ex) {
            return List.of("Response does not conform to required schema");
        }

        // Passed all checks
        return List.of();
    }

    @Override
    public String getValidationFailureRetryPrompt(String responseFailingValidation, List<String> validationErrors) {
        return String.format("Your response does not meet the requirements: %s. Correct your response and return only the JSON " +
                "data with no other commentary or explanation", String.join(";", validationErrors));
    }

    @Override
    public String sanitizeResponse(String response) {
        if (StringUtils.isBlank(response)) return response;

        // In case the model has ignored the "only the JSON" constraint, attempt to locate the largest JSON block within the response
        final var matcher = JSON_EXTRACT.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return response;
    }

    @Override
    public String generateTransformedOutput(String response) {
        final var nodeData = Util.deserializeOrThrow(response, IntermediateModel.class,
                e -> new RuntimeException("Failed to deserialize last response into required data: " + e.getMessage(), e));

        final var model = generator.generateModel(nodeData);
        final var serialized = Bpmn.convertToString(model.getValue());

        return serialized;
    }

    public static List<String> validateSchemaAndReturnErrors(String schemaContent, String data) {
        try {
            JSONObject rawSchema = new JSONObject(schemaContent);
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(new JSONObject(data)); // Throws a ValidationException if this object is invalid

            return List.of();
        }
        catch (ValidationException ex) {
            return ex.getAllMessages();
        }
    }
}
