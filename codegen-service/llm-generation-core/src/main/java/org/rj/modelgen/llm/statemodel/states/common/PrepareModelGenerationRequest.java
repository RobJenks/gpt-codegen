package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.prompt.PromptSubstitution;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
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
        recordAudit("prompt", prompt);

        final var newContext = contextProvider.withPrompt(context, prompt);
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
        substitutionData.put(StandardPromptPlaceholders.SCHEMA_CONTENT.getValue(), modelSchema.getSchemaContent());
        substitutionData.put(StandardPromptPlaceholders.CURRENT_STATE.getValue(), context.getLatestModelEntry()
                .orElseGet(() -> ContextEntry.forModel("{}")).getContent());

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
     * @param modelSchema       Intermediate model schema
     * @param context           Current context for this session
     * @param request           Request received from the caller
     * @param substitutions     The list of substitutions to be applied when generating this prompt
     *
     * @return                  Prompt for LLM submission
     */
    protected abstract String buildGenerationPrompt(ModelSchema modelSchema, Context context, String request, List<PromptSubstitution> substitutions);
}
