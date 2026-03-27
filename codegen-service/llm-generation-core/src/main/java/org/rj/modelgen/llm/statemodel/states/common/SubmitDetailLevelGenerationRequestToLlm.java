package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceStandardSignals;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.validation.ResponseSanitizer;

import java.util.Optional;

public class SubmitDetailLevelGenerationRequestToLlm extends SubmitGenerationRequestToLlm {
    private static final int MAX_LLM_DIRECTED_RETRIES = 3;
    private final int maxLlmDirectedRetries;

    public SubmitDetailLevelGenerationRequestToLlm(ResponseSanitizer modelSanitizer) {
        this(SubmitDetailLevelGenerationRequestToLlm.class, modelSanitizer, MAX_LLM_DIRECTED_RETRIES);
    }

    public SubmitDetailLevelGenerationRequestToLlm(Class<? extends SubmitGenerationRequestToLlm> cls, ResponseSanitizer modelSanitizer) {
        this(cls, modelSanitizer, MAX_LLM_DIRECTED_RETRIES);
    }

    public SubmitDetailLevelGenerationRequestToLlm(Class<? extends SubmitGenerationRequestToLlm> cls, ResponseSanitizer modelSanitizer, int maxLlmDirectedRetries) {
        super(cls, modelSanitizer);
        this.maxLlmDirectedRetries = maxLlmDirectedRetries;
    }

    @Override
    public String getDescription() {
        return "Submit detail-level generation request to LLM";
    }

    @Override
    protected Optional<ModelInterfaceSignal> processRawResponse(ModelResponse response) {
        // Only process valid, successful responses
        if (response == null) return Optional.empty();
        if (!response.isSuccessful()) return Optional.empty();
        if (response.getMessage() == null) return Optional.empty();

        // Handle LLM-directed retry signal from detail-level generation
        final var content = response.getMessage();
        final var retryMarker = StandardPromptPlaceholders.RETURN_TO_HIGH_LEVEL.getValue();
        if (content.contains(retryMarker)) {
            final var reason = content
                    .replace(String.format("%s:", retryMarker), "")
                    .replace(retryMarker, "")
                    .strip();
            getPayload().put(MultiLevelModelStandardPayloadData.LlmDirectedRetryReason, reason);

            final var currentRetryCount = (Integer) getPayload().getData().getOrDefault(
                    MultiLevelModelStandardPayloadData.LlmDirectedRetryCount.toString(), 0);
            final var newRetryCount = currentRetryCount + 1;

            if (newRetryCount > maxLlmDirectedRetries) {
                return Optional.of(new ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS(
                        "Reached maximum number of retry invocations between states",
                        newRetryCount));
            }

            getPayload().put(MultiLevelModelStandardPayloadData.LlmDirectedRetryCount, newRetryCount);

            // Retry high-level generation when doing one-shot generation
            // Retry detail-level generation when doing multi-shot generation
            if (getPayload().hasData(StandardModelData.CanvasModel.toString())) {
                return Optional.of(outboundSignal(MultiLevelModelStandardSignals.ReturnToHighLevel));
            } else {
                return Optional.of(outboundSignal(MultiLevelModelStandardSignals.RetryDetailLevel));
            }
        }

        // Otherwise no special handling, so return to normal processing
        return Optional.empty();
    }
}
