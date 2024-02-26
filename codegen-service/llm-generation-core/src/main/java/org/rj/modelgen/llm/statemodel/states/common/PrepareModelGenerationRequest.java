package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.provider.ContextProvider;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.prompt.PromptGenerator;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import reactor.core.publisher.Mono;


public abstract class PrepareModelGenerationRequest extends ModelInterfaceState implements CommonStateInterface {
    private final ModelSchema modelSchema;
    private final ContextProvider contextProvider;

    public PrepareModelGenerationRequest(Class<? extends PrepareModelGenerationRequest> cls, ModelSchema modelSchema, ContextProvider contextProvider) {
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

        final var prompt = buildGenerationPrompt(modelSchema, context, request);

        final var newContext = contextProvider.withPrompt(context, prompt);
        getModelInterface().getOrCreateSession(sessionId).replaceContext(newContext);

        return outboundSignal(getSuccessSignalId())
                .withPayloadData(StandardModelData.Context, newContext)
                .mono();
    }

    /**
     * Implemented by subclasses.  Generates the new prompt based on the given context and other supporting data
     * @param modelSchema       Intermediate model schema
     * @param context           Current context for this session
     * @param request           Request received from the caller
     *
     * @return                  Prompt for LLM submission
     */
    protected abstract String buildGenerationPrompt(ModelSchema modelSchema, Context context, String request);
}
