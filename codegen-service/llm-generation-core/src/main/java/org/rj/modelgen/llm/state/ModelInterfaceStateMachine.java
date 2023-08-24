package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.exception.LlmGenerationConfigException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelInterfaceStateMachine {
    private final Map<String, ModelInterfaceState<? extends ModelInterfaceSignal>> states;
    private final ModelInterfaceTransitionRules rules;

    // Default states built in to all models
    private final ModelInterfaceState<? extends ModelInterfaceSignal> defaultStateNoRule = new ModelInterfaceStandardStates.NO_TRANSITION_RULE();
    private final ModelInterfaceState<? extends ModelInterfaceSignal> defaultStateMaxInvocations = new ModelInterfaceStandardStates.EXCEEDED_MAX_INVOCATIONS();

    public ModelInterfaceStateMachine(List<ModelInterfaceState<? extends ModelInterfaceSignal>> states, ModelInterfaceTransitionRules rules) {
        this.states = Optional.ofNullable(states).orElseGet(List::of).stream()
                .collect(Collectors.toMap(ModelInterfaceState::getId, Function.identity(),
                        (a, b) -> { throw new IllegalArgumentException("Cannot build model; invalid duplicate state ID: " + a.getId()); }));
        this.rules = Optional.ofNullable(rules).orElseGet(() -> new ModelInterfaceTransitionRules(List.of()));
    }

    public Mono<ModelInterfaceExecutionResult> execute(String initialState) {
        return execute(initialState, new ModelInterfaceStandardSignals.EMPTY());
    }

    public <TSignal extends ModelInterfaceSignal>
    Mono<ModelInterfaceExecutionResult> execute(String initialState, TSignal inputSignal) {
        final var init = Optional.ofNullable(states).map(x -> x.get(initialState))
                .orElseThrow(() -> new LlmGenerationConfigException(String.format("Cannot start execution; initial state '%s' not found", initialState)));

        final Mono<List<ModelInterfaceStateWithInputSignal>> execution = Mono.just(new ModelInterfaceStateWithInputSignal(init, inputSignal))
                .expand(this::executeStep)
                .collectList();

        return execution.map(this::buildResult);
    }

    private Mono<ModelInterfaceStateWithInputSignal<? extends ModelInterfaceSignal>>
    executeStep(ModelInterfaceStateWithInputSignal<? extends ModelInterfaceSignal> input) {
        // If this is a terminal state then end the execution here
        if (input.getState().isTerminal()) return Mono.empty();

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

                            // No matching transition rule.  Redirect to the built-in failure node
                            .orElseGet(() -> new ModelInterfaceStateWithInputSignal(defaultStateNoRule,
                                new ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE(input.getState().getId(), outputSignal.getId())));
                    });
    }

    private ModelInterfaceExecutionResult buildResult(List<ModelInterfaceStateWithInputSignal> steps) {
        return new ModelInterfaceExecutionResult(
                steps.get(steps.size() - 1).getState(),
                steps
        );
    }
}
