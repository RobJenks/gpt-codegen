package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import reactor.core.publisher.Mono;

/**
 * Model state which wraps two component states, (Prepare request + Submit request to LLM).  Used to simplify
 * more complex models which may require many levels or iterations of LLM submission
 */
public abstract class PrepareAndSubmitLlmGenerationRequest extends ModelInterfaceState implements CommonStateInterface {
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
                    // Invoke submission phase, or early-exit if preparation ended in error
                    if (prepareRequestPhase.getSuccessSignalId().equals(prepareResult.getId())) {
                        return submitRequestPhase.invoke(prepareResult);
                    }
                    else {
                        return Mono.just(prepareResult);
                    }
                });
    }
}
