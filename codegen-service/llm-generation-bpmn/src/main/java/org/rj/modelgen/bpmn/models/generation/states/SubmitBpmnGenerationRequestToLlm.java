package org.rj.modelgen.bpmn.models.generation.states;

import org.jooq.lambda.tuple.Tuple;
import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.request.ModelRequestHttpOptions;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;
import reactor.core.publisher.Mono;
import static org.jooq.lambda.tuple.Tuple.*;
import static org.rj.modelgen.llm.util.FuncUtil.*;

public class SubmitBpmnGenerationRequestToLlm extends ModelInterfaceState<LlmModelRequestPreparedSuccessfully> {
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
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);
        if (input.getContext() == null) throw new BpmnGenerationException("No valid context for LLM submission");

        final var request = new ModelRequest("gpt-4", 0.7f, input.getContext());
        return getModelInterface().submit(input.getSessionId(), request, getHttpOptions(inputSignal))
                .map(response -> tuple(response, sanitizer.sanitize(response.getMessage())))
                .map(res -> doVoid(res, responseAndSanitizedContent ->
                        recordModelResponse(input.getSessionId(), responseAndSanitizedContent.v1, responseAndSanitizedContent.v2)))
                .flatMap(responseAndSanitizedContent -> outboundSignal(new LlmResponseReceived(input.getSessionId(), responseAndSanitizedContent.v1, responseAndSanitizedContent.v2)));
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
