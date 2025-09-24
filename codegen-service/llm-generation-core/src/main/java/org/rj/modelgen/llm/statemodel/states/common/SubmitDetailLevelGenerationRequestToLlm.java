package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.prompt.StandardPromptPlaceholders;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.validation.ResponseSanitizer;

import java.util.Optional;

public class SubmitDetailLevelGenerationRequestToLlm extends SubmitGenerationRequestToLlm {
    public SubmitDetailLevelGenerationRequestToLlm(ResponseSanitizer modelSanitizer) {
        this(SubmitDetailLevelGenerationRequestToLlm.class, modelSanitizer);
    }

    public SubmitDetailLevelGenerationRequestToLlm(Class<? extends SubmitGenerationRequestToLlm> cls, ResponseSanitizer modelSanitizer) {
        super(cls, modelSanitizer);
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

        // Handle LLM-directed retry signal from detail-level generation, which requires execution return to high-level
        final var content = response.getMessage();
        final var retryMarker = StandardPromptPlaceholders.RETURN_TO_HIGH_LEVEL.getValue();
        if (content.contains(retryMarker)) {
            final var reason = content
                    .replace(String.format("%s:", retryMarker), "")
                    .replace(retryMarker, "")
                    .strip();
            getPayload().put(MultiLevelModelStandardPayloadData.LlmDirectedRetryReason, reason);

            return Optional.of(outboundSignal(MultiLevelModelStandardSignals.ReturnToHighLevel));
        }

        // Otherwise no special handling, so return to normal processing
        return Optional.empty();
    }
}
