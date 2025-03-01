package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.models.generation.options.GenerationModelOptionsImpl;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.Objects;
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
        this.prepareRequestPhase = Objects.requireNonNull(prepareRequestPhase);
        this.submitRequestPhase = Objects.requireNonNull(submitRequestPhase);
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

    @Override
    public void completeStateInitialization() {
        // Override child state IDs, unless they have been explicitly overridden already
        if (!prepareRequestPhase.hasOverriddenId()) prepareRequestPhase.overrideDefaultId(getPrepareStateId());
        if (!submitRequestPhase.hasOverriddenId()) submitRequestPhase.overrideDefaultId(getSubmitStateId());
    }

    @Override
    public <T extends GenerationModelOptionsImpl<T>> void applyModelOptions(GenerationModelOptionsImpl<T> options) {
        // Compound state needs to distribute any options affecting this state (based on its ID) to child states
        // Generate a new options object containing that mapping which is passed down to the child states, since
        // we don't want this mapping to exist outside of this compound object
        final var newOptions = Util.cloneObject(options);
        if (newOptions.hasOverriddenLlmResponse(getId())) {
            final var response = newOptions.getOverriddenLlmResponse(getId());
            newOptions.addOverriddenLlmResponse(getSubmitStateId(), response.getResponse(), response.getStatus());
        }

        // Distribute to child states
        prepareRequestPhase.applyModelOptions(newOptions);
        submitRequestPhase.applyModelOptions(newOptions);
    }

    private String getPrepareStateId() {
        return String.format("%s.prepare", getId());
    }

    private String getSubmitStateId() {
        return String.format("%s.submit", getId());
    }

    private void setResponseContentOutputKey(String outputKey) {
        submitRequestPhase.setResponseContentOutputKey(outputKey);
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelResponse(ModelResponse response) {
        submitRequestPhase.withOverriddenModelResponse(response);
        return this;
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelSuccessResponse(String response) {
        submitRequestPhase.withOverriddenModelSuccessResponse(response);
        return this;
    }

    public PrepareAndSubmitLlmGenerationRequest withOverriddenModelFailureResponse(String error) {
        submitRequestPhase.withOverriddenModelFailureResponse(error);
        return this;
    }
}
