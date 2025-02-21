package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Model state which wraps two component states, (Prepare request + Submit request to LLM).  Used to simplify
 * more complex models which may require many levels or iterations of LLM submission
 */
public class PrepareAndSubmitLlmGenerationRequest extends ModelInterfaceState implements CommonStateInterface {
    private final PrepareModelGenerationRequest prepareRequestPhase;
    private final SubmitGenerationRequestToLlm submitRequestPhase;

    public PrepareAndSubmitLlmGenerationRequest(Class<? extends PrepareAndSubmitLlmGenerationRequest> cls,
                                                PrepareModelGenerationRequest prepareRequestPhase,
                                                SubmitGenerationRequestToLlm submitRequestPhase) {
        super(cls);
        this.prepareRequestPhase = prepareRequestPhase;
        this.submitRequestPhase = submitRequestPhase;
    }

    @Override
    public String getDescription() {
        return "Prepare and submit model generation request to LLM";
    }

    @Override
    public void registerWithModel(ModelInterfaceStateMachine model) {
        super.registerWithModel(model);
        prepareRequestPhase.registerWithModel(model);
        submitRequestPhase.registerWithModel(model);
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        // Invoke prepare phase
        return prepareRequestPhase.invoke(inputSignal)
                .flatMap(prepareResult -> {

                    // Prompt generation was skipped (likely because no prompt is defined for this stage) so skip
                    // the entire prepare+submit stage.  Model transition rules should  decide how to handle
                    // the SKIPPED output signal
                    if (StandardSignals.SKIPPED.equals(prepareResult.getId())) {
                        return outboundSignal(StandardSignals.SKIPPED, String.format("Skipped (%s)",
                                Optional.ofNullable(prepareResult.getDescription()).orElse("Prepare phase was skipped")))
                                .mono();
                    }

                    // Prompt generation was successful, so invoke "execute" phase
                    else if (prepareRequestPhase.getSuccessSignalId().equals(prepareResult.getId())) {
                        return submitRequestPhase.invoke(prepareResult)
                                .map(executeResult -> {
                                    if (submitRequestPhase.getSuccessSignalId().equals(executeResult.getId())) {
                                        return outboundSignal(getSuccessSignalId(), executeResult.getDescription())
                                                .withPayloadData(executeResult.getPayload());
                                    }
                                    else {
                                        return executeResult;
                                    }
                                });
                    }

                    // Prompt generation failed for some other reason
                    else {
                        return outboundSignal(prepareResult.getId(),
                                Optional.ofNullable(prepareResult.getDescription()).orElse("Prepare phase did not succeed"))
                                .mono();
                    }
                });
    }


    public PrepareAndSubmitLlmGenerationRequest withResponseOutputKey(String outputKey) {
        setResponseContentOutputKey(outputKey);
        return this;
    }
    public<T extends Enum<?>> PrepareAndSubmitLlmGenerationRequest withResponseOutputKey(T outputKey) {
        setResponseContentOutputKey(outputKey.toString());
        return this;
    }

    private void setResponseContentOutputKey(String outputKey) {
        if (submitRequestPhase != null) {
            submitRequestPhase.setResponseContentOutputKey(outputKey);
        }
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelResponse(ModelResponse response) {
        if (submitRequestPhase != null) {
            submitRequestPhase.withOverriddenModelResponse(response);
        }

        return this;
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelSuccessResponse(String response) {
        if (submitRequestPhase != null) {
            submitRequestPhase.withOverriddenModelSuccessResponse(response);
        }

        return this;
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelFailureResponse(String error) {
        if (submitRequestPhase != null) {
            submitRequestPhase.withOverriddenModelFailureResponse(error);
        }

        return this;
    }
}
