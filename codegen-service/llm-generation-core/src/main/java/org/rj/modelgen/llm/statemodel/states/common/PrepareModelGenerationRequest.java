package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;


public abstract class PrepareModelGenerationRequest extends ModelInterfaceState implements CommonStateInterface {
    private final ModelSchema modelSchema;
    private final ContextProvider contextProvider;

    public PrepareModelGenerationRequest(Class<? extends PrepareModelGenerationRequest> cls, ModelSchema modelSchema,
                                         ContextProvider contextProvider) {
        super(cls);
        this.modelSchema = modelSchema;
        this.contextProvider = contextProvider;
    }

    @Override
    public String getDescription() {
        return "Prepare model generation request";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {
        final Context context = getPayload().getOrElse(StandardModelData.Context, contextProvider::newContext);
        final String request = getPayload().getOrThrow(StandardModelData.Request, () -> new LlmGenerationModelException("No valid request provided"));
        final String sessionId = getPayload().getOrThrow(StandardModelData.SessionId, () -> new LlmGenerationModelException("No valid session ID for request"));

        final var substitutions = generatePromptSubstitutions(modelSchema, context, request);
        final var prompt = buildGenerationPrompt(modelSchema, context, request, substitutions);

        if (prompt.isEmpty()) {
            return outboundSignal(StandardSignals.SKIPPED, "No prompt generated for this state").mono();
        }

        recordAudit("prompt", prompt.get());

        final var newContext = contextProvider.withPrompt(context, prompt.get());
        getModelInterface().getOrCreateSession(sessionId).replaceContext(newContext);

        return outboundSignal(getSuccessSignalId())
                .withPayloadData(StandardModelData.Context, newContext)
                .mono();
    }

    protected List<PromptSubstitution> generatePromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        // Expose all payload variables to the templates (first level only; no need to support nested objects at this point)
        final Map<String, String> substitutionData = getPayload().getData().entrySet().stream()
                .filter(e -> Objects.nonNull(e.getKey()) && Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

        // Add core fields
        substitutionData.put(StandardPromptPlaceholders.PROMPT.getValue(), request);
        substitutionData.put(StandardPromptPlaceholders.SCHEMA_CONTENT.getValue(), getSchemaContent(modelSchema));
        substitutionData.put(StandardPromptPlaceholders.CURRENT_STATE.getValue(), context.getLatestModelEntry()
                .orElseGet(() -> ContextEntry.forModel(null)).getContent());

        // Allow subclasses to insert additional data.  Will overwrite exiting placeholders if they exist
        final var substitutions = substitutionData.entrySet().stream()
                .map(e -> new PromptSubstitution(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        final var additional = generateAdditionalPromptSubstitutions(modelSchema, context, request);
        substitutions.addAll(Optional.ofNullable(additional).orElseGet(List::of));

        return substitutions;
    }

    /**
     * Can be overridden by subclasses to insert new substitutions into the preparation phase
     *
     * @param modelSchema       Intermediate model schema
     * @param context           Current context for this session
     * @param request           Request received from the caller
     *
     * @return                  The additional substitutions to be included when preparing this request
     */
    protected List<PromptSubstitution> generateAdditionalPromptSubstitutions(ModelSchema modelSchema, Context context, String request) {
        return List.of();
    }

    /**
     * Implemented by subclasses.  Generates the new prompt based on the given context and other supporting data
     * Will optionally return a prompt depending on whether it is possible or necessary to generate one for this
     * execution of the model.  E.g. some stages may be optional depending on the model or the specific input
     * received from the previous state
     *
     * @param modelSchema       Intermediate model schema
     * @param context           Current context for this session
     * @param request           Request received from the caller
     * @param substitutions     The list of substitutions to be applied when generating this prompt
     *
     * @return                  Prompt for LLM submission, or Optional.empty() if no prompt should/can be generated
     *                          Optional.empty() is only returned in cases where the prompt is not generate for known reasons,
     *                          e.g. because it is not necessary, or the state is optional and no prompt template exists for this
     *                          implementation of the model.  This stage will still return an error signal if a prompt is not generated
     *                          due to a FAILURE
     */
    protected abstract Optional<String> buildGenerationPrompt(ModelSchema modelSchema, Context context, String request, List<PromptSubstitution> substitutions);

    // Model schema is optional
    private String getSchemaContent(ModelSchema schema) {
        return Optional.ofNullable(schema)
                .map(ModelSchema::getSchemaContent)
                .orElseGet(() -> "{}");
    }
}
