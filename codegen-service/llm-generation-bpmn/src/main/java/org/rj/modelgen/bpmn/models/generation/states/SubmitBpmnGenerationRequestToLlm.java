package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;
import reactor.core.publisher.Mono;
import static org.jooq.lambda.tuple.Tuple.*;
import static org.rj.modelgen.llm.util.FuncUtil.*;

public class SubmitBpmnGenerationRequestToLlm extends ModelInterfaceState {
    private final IntermediateModelSanitizer sanitizer;

    public SubmitBpmnGenerationRequestToLlm() {
        super(SubmitBpmnGenerationRequestToLlm.class);
        this.sanitizer = new IntermediateModelSanitizer();
    }

    @Override
    public String getDescription() {
        return "Submit new BPMN generation request to LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {
        final Context context = input.getPayload().get(StandardModelData.Context);
        if (context == null) throw new BpmnGenerationException("No valid context for LLM submission");

        final String sessionId = input.getPayload().get(StandardModelData.SessionId);
        if (sessionId == null) throw new BpmnGenerationException("No valid session ID for LLM submission");

        final var request = new ModelRequest(
                getPayload().getOrElse(StandardModelData.Llm, "gpt-4"),
                getPayload().getOrElse(StandardModelData.Temperature, 0.7),
                context);

        return getModelInterface().submit(sessionId, request, getHttpOptions(input))
                .map(response -> tuple(response, sanitizer.sanitize(response.getMessage())))
                .map(res -> doVoid(res, responseAndSanitizedContent ->
                        recordModelResponse(sessionId, responseAndSanitizedContent.v1, responseAndSanitizedContent.v2)))

                .flatMap(responseAndSanitizedContent -> outboundSignal(BpmnGenerationSignals.ValidateLlmResponse)
                        .withPayloadData(StandardModelData.ModelResponse, responseAndSanitizedContent.v1)
                        .withPayloadData(StandardModelData.SanitizedContent, responseAndSanitizedContent.v2)
                        .mono());
    }

    protected ModelRequestHttpOptions getHttpOptions(ModelInterfaceSignal inputSignal) {
        return new ModelRequestHttpOptions();
    }

    private void recordModelResponse(String sessionId, ModelResponse modelResponse, String sanitizedContent) {
        getModelInterface().getOrCreateSession(sessionId)
                .getContext().addModelResponse(sanitizedContent);

        // TODO: Record token usage etc.
    }
}
