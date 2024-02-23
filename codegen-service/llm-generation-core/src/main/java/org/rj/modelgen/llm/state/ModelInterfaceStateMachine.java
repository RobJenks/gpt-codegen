package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.exception.LlmGenerationConfigException;
import org.rj.modelgen.llm.model.ModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelInterfaceStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(ModelInterfaceStateMachine.class);

    private final ModelInterface modelInterface;
    private final Map<String, ModelInterfaceState> states;
    private final ModelInterfaceTransitionRules rules;

    // Default states built in to all models
    private final ModelInterfaceState defaultStateError = new ModelInterfaceStandardStates.FAILED_WITH_ERROR();
    private final ModelInterfaceState defaultStateNoRule = new ModelInterfaceStandardStates.NO_TRANSITION_RULE();
    private final ModelInterfaceState defaultStateMaxInvocations = new ModelInterfaceStandardStates.EXCEEDED_MAX_INVOCATIONS();

    public ModelInterfaceStateMachine(ModelInterface modelInterface, List<ModelInterfaceState> states,
                                      ModelInterfaceTransitionRules rules) {
        this.modelInterface = modelInterface;
        this.states = Optional.ofNullable(states).orElseGet(List::of).stream()
                .collect(Collectors.toMap(ModelInterfaceState::getId, Function.identity(),
                        (a, b) -> { throw new IllegalArgumentException("Cannot build model; invalid duplicate state ID: " + a.getId()); }));
        this.rules = Optional.ofNullable(rules).orElseGet(() -> new ModelInterfaceTransitionRules(List.of()));

        this.states.values().forEach(state -> state.registerWithModel(this));
    }

    public <TPayload extends ModelInterfaceDataPayload>
    Mono<ModelInterfaceExecutionResult> execute(String initialState, String inputSignal, TPayload payload) {
        LOG.info("Executing state model interface from initial state '{}'", initialState);
        final var init = Optional.ofNullable(states).map(x -> x.get(initialState))
                .orElseThrow(() -> new LlmGenerationConfigException(String.format("Cannot start execution; initial state '%s' not found", initialState)));

        final var startSignal = new ModelInterfaceStartSignal<>(inputSignal, payload);

        final Mono<List<ModelInterfaceStateWithInputSignal>> execution = Mono.just(new ModelInterfaceStateWithInputSignal(init, startSignal))
                .expand(this::executeStep)
                .collectList();

        return execution.map(this::buildResult);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Mono<ModelInterfaceStateWithInputSignal>
    executeStep(ModelInterfaceStateWithInputSignal input) {
        LOG.info("Model interface executing state '{}' with input signal '{}'",
                input.getState().getId(), input.getInputSignal());

        // If this is a terminal state then invoke it and end the execution immediately
        if (input.getState().isTerminal()) {
            return input.getState().invoke(input.getInputSignal())
                    .flatMap(__ -> Mono.empty());   // No output signal from a terminal state
        }

        // Execute the action associated with this state
        return input.getState().invoke(input.getInputSignal())
                .map(outputSignal -> {
                    // Terminate execution if we exceeded the maximum allowed invocations of a state
                    if (ModelInterfaceSignal.defaultSignalId(ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS.class)
                            .equals(outputSignal.getId())) {
                        return new ModelInterfaceStateWithInputSignal(defaultStateMaxInvocations, outputSignal);
                    }

                    // Attempt to find a matching rule based on this state and the action output
                    return rules.find(input.getState(), outputSignal)

                            // Transition rule exists; move to the next step
                            .map(rule -> new ModelInterfaceStateWithInputSignal(rule.getNextState(), outputSignal))

                            // No matching transition rule
                            .orElseGet(() ->
                                    // Special-case: route any unhandled error signals to the global error handler state
                                    outputSignal.getAs(ModelInterfaceStandardSignals.GENERAL_ERROR.class)
                                            .map(error -> new ModelInterfaceStateWithInputSignal(defaultStateError, outputSignal))

                                    // Not an error, so route to the 'no matching rule' end state
                                    .orElseGet(() -> new ModelInterfaceStateWithInputSignal(defaultStateNoRule,
                                            new ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE(input.getState().getId(), outputSignal.getId())))
                            );
                    });
    }

    private ModelInterfaceExecutionResult buildResult(List<ModelInterfaceStateWithInputSignal> steps) {
        return new ModelInterfaceExecutionResult(
                steps.get(steps.size() - 1).getState(),
                steps
        );
    }

    public ModelInterface getModelInterface() {
        return modelInterface;
    }
}
