package org.rj.modelgen.llm.statemodel.states.common;

import org.json.JSONObject;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;
import org.rj.modelgen.llm.models.generation.options.OverriddenLlmResponse;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.util.Util;
import org.rj.modelgen.llm.validation.ResponseSanitizer;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.rj.modelgen.llm.util.FuncUtil.doVoid;

public class SubmitGenerationRequestToLlm extends ModelInterfaceState implements CommonStateInterface {
    private final ResponseSanitizer sanitizer;

    // Can be set to explicitly output the LLM response to a specific key in the output signal payload, in
    // addition to the default behavior of outputting to `responseContent`
    private String responseContentOutputKey;

    // Can be set to shortcut LLM submission and return an overridden model response.  Responses can be conditional.
    // First matching override will be selected
    private List<OverriddenLlmResponse> responseOverrides;

    public SubmitGenerationRequestToLlm(ResponseSanitizer modelSanitizer) {
        this(SubmitGenerationRequestToLlm.class, modelSanitizer);
    }

    public SubmitGenerationRequestToLlm(Class<? extends SubmitGenerationRequestToLlm> cls, ResponseSanitizer modelSanitizer) {
        super(cls);
        this.sanitizer = modelSanitizer;
    }

    @Override
    public String getDescription() {
        return "Submitting new generation request to LLM";
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
                .map(response -> {
                    final var rawResponseResult = processRawResponse(response);
                    if (rawResponseResult.isPresent()) return rawResponseResult.get();

                    final var sanitized = sanitizeResponse(response.getMessage());
                    recordModelResponse(sessionId, response, sanitized);

                    return outboundSignal(getSuccessSignalId())
                            .withPayloadData(StandardModelData.ModelResponse, response)
                            .withPayloadData(StandardModelData.ResponseContent, sanitized)
                            .withPayloadData(addExplicitOutputPayloadIfRequired(sanitized));
                });
    }

    private Mono<ModelResponse> submitRequest(String sessionId, ModelRequest request, ModelInterfacePayload payload) {
        final var override = Optional.ofNullable(responseOverrides).orElseGet(List::of).stream()
                .filter(ov -> ov.isApplicable(getPayload()))
                .findFirst();

        if (override.isPresent()) {
            return Mono.just(override.get().generateModelResponse());
        }

        return getModelInterface().submit(sessionId, request, payload);
    }

    // Can be implemented by subclasses.  Process the raw response before any sanitization or validation is performed, and
    // optionally return an outbound signal which the state should emit.  If a signal is provided the state will take no
    // further action.  If none is provided (default) the state will sanitize and validate the response as normal.  Can
    // be used by subclasses to test for certain special responses which should be handled differently, or otherwise delegate
    // to the normal response processing logic
    protected Optional<ModelInterfaceSignal> processRawResponse(ModelResponse response) {
        return Optional.empty();
    }

    private void recordModelResponse(String sessionId, ModelResponse modelResponse, String sanitizedContent) {
        getModelInterface().getOrCreateSession(sessionId)
                .getContext().addModelResponse(sanitizedContent);

        recordAudit("response", Util.trySerializeJson(modelResponse).map(JSONObject::toString).orElseDefault("<error>"));
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
            addModelResponseOverrides(options.getOverriddenLlmResponses(getId()));
        }
    }

    private void setModelResponseOverrides(List<OverriddenLlmResponse> responses) {
        this.responseOverrides = responses;
    }

    private void addModelResponseOverride(OverriddenLlmResponse response) {
        if (this.responseOverrides == null) {
            this.responseOverrides = new ArrayList<>();
        }
        this.responseOverrides.add(response);
    }

    private void addModelResponseOverrides(List<OverriddenLlmResponse> responses) {
        if (responses == null) return;
        responses.forEach(this::addModelResponseOverride);
    }

    public SubmitGenerationRequestToLlm withOverriddenModelResponses(List<OverriddenLlmResponse> responses) {
        setModelResponseOverrides(responses);
        return this;
    }

    public SubmitGenerationRequestToLlm withOverriddenModelResponse(OverriddenLlmResponse response) {
        addModelResponseOverride(response);
        return this;
    }

    public SubmitGenerationRequestToLlm withOverriddenModelSuccessResponse(String response) {
        final var override = new OverriddenLlmResponse(response, ModelResponse.Status.SUCCESS);
        return withOverriddenModelResponse(override);
    }

    public SubmitGenerationRequestToLlm withOverriddenModelFailureResponse(String error) {
        final var override = new OverriddenLlmResponse(error, ModelResponse.Status.FAILED);
        return withOverriddenModelResponse(override);
    }
}
