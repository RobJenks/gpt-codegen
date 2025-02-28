package org.rj.modelgen.llm.statemodel.states.common;

import org.json.JSONObject;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.models.generation.options.GenerationModelOptions;
import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.util.Util;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;
import reactor.core.publisher.Mono;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.rj.modelgen.llm.util.FuncUtil.doVoid;

public class SubmitGenerationRequestToLlm extends ModelInterfaceState implements CommonStateInterface {
    private final IntermediateModelSanitizer sanitizer;

    // Can be set to explicitly output the LLM response to a specific key in the output signal payload, in
    // addition to the default behavior of outputting to `responseContent`
    private String responseContentOutputKey;
    // Can be set to shortcut LLM submission and return an overridden model response
    private ModelResponse responseOverride;

    public SubmitGenerationRequestToLlm(IntermediateModelSanitizer modelSanitizer) {
        this(SubmitGenerationRequestToLlm.class, modelSanitizer);
    }

    public SubmitGenerationRequestToLlm(Class<? extends SubmitGenerationRequestToLlm> cls, IntermediateModelSanitizer modelSanitizer) {
        super(cls);
        this.sanitizer = modelSanitizer;
    }

    @Override
    public String getDescription() {
        return "Submit new generation request to LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {
        final Context context = input.getPayload().get(StandardModelData.Context);
        if (context == null) throw new LlmGenerationModelException("No valid context for LLM submission");

        final String sessionId = input.getPayload().get(StandardModelData.SessionId);
        if (sessionId == null) throw new LlmGenerationModelException("No valid session ID for LLM submission");

        final var request = new ModelRequest(
                getPayload().getOrElse(StandardModelData.Llm, "gpt-4"),
                getPayload().getOrElse(StandardModelData.Temperature, 0.7),
                context);

        return submitRequest(sessionId, request, getPayload())
                .map(response -> tuple(response, sanitizeResponse(response.getMessage())))
                .map(res -> doVoid(res, responseAndSanitizedContent ->
                        recordModelResponse(sessionId, responseAndSanitizedContent.v1, responseAndSanitizedContent.v2)))

                .flatMap(responseAndSanitizedContent -> outboundSignal(getSuccessSignalId())
                        .withPayloadData(StandardModelData.ModelResponse, responseAndSanitizedContent.v1)
                        .withPayloadData(StandardModelData.ResponseContent, responseAndSanitizedContent.v2)
                        .withPayloadData(addExplicitOutputPayloadIfRequired(responseAndSanitizedContent.v2))
                        .mono());
    }

    private Mono<ModelResponse> submitRequest(String sessionId, ModelRequest request, ModelInterfacePayload payload) {
        if (responseOverride != null) {
            return Mono.just(responseOverride);
        }

        return getModelInterface().submit(sessionId, request, payload);
    }

    private void recordModelResponse(String sessionId, ModelResponse modelResponse, String sanitizedContent) {
        getModelInterface().getOrCreateSession(sessionId)
                .getContext().addModelResponse(sanitizedContent);

        recordAudit("response", Util.trySerializeJson(modelResponse).map(JSONObject::toString).orElse("<error>"));
        recordAudit("response-content", sanitizedContent);

        // TODO: Record token usage etc.
    }

    private String sanitizeResponse(String response) {
        // Response sanitizer is optional
        if (sanitizer == null) {
            return response;
        }

        return sanitizer.sanitize(response);
    }

    private ModelInterfacePayload addExplicitOutputPayloadIfRequired(String responseContent) {
        if (responseContentOutputKey == null) return null;

        return new ModelInterfacePayload()
                .withData(responseContentOutputKey, responseContent);
    }

    public SubmitGenerationRequestToLlm withResponseOutputKey(String outputKey) {
        setResponseContentOutputKey(outputKey);
        return this;
    }

    public void setResponseContentOutputKey(String outputKey) {
        this.responseContentOutputKey = outputKey;
    }

    @Override
    public <T extends GenerationModelOptionsImpl<T>> void applyModelOptions(GenerationModelOptionsImpl<T> options) {
        if (options == null) return;

        // Override model response if required
        if (options.hasOverriddenLlmResponse(getId())) {
            final var response = options.getOverriddenLlmResponse(getId());
            switch (response.getStatus()) {
                case SUCCESS -> overrideWithModelSuccessResponse(response.getResponse());
                case FAILED -> overrideWithModelFailureResponse(response.getResponse());
            }
        }
    }

    private void overrideWithModelSuccessResponse(String response) {
        final var modelResponse = new ModelResponse();
        modelResponse.setStatus(ModelResponse.Status.SUCCESS);
        modelResponse.setMessage(response);

        overrideModelResponse(modelResponse);
    }

    private void overrideWithModelFailureResponse(String error) {
        final var modelResponse = new ModelResponse();
        modelResponse.setStatus(ModelResponse.Status.FAILED);
        modelResponse.setError(error);

        overrideModelResponse(modelResponse);
    }

    private void overrideModelResponse(ModelResponse response) {
        this.responseOverride = response;
    }

    public SubmitGenerationRequestToLlm withOverriddenModelResponse(ModelResponse response) {
        overrideModelResponse(response);
        return this;
    }

    public SubmitGenerationRequestToLlm withOverriddenModelSuccessResponse(String response) {
        overrideWithModelSuccessResponse(response);
        return this;
    }

    public SubmitGenerationRequestToLlm withOverriddenModelFailureResponse(String error) {
        overrideWithModelFailureResponse(error);
        return this;
    }
}
