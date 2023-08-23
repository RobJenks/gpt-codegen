package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.exception.LlmGenerationConfigException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelInterfaceStateMachine {
    private final Map<String, ModelInterfaceState> states;
    private final ModelInterfaceTransitionRules rules;

    // Default states built in to all models
    private final ModelInterfaceState defaultStateNoRule = new ModelInterfaceStandardStates.NO_TRANSITION_RULE();

    public ModelInterfaceStateMachine(List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
        this.states = Optional.ofNullable(states).orElseGet(List::of).stream()
                .collect(Collectors.toMap(ModelInterfaceState::getId, Function.identity(),
                        (a, b) -> { throw new IllegalArgumentException("Cannot build model; invalid duplicate state ID: " + a.getId()); }));
        this.rules = Optional.ofNullable(rules).orElseGet(() -> new ModelInterfaceTransitionRules(List.of()));
    }

    public Mono<ModelInterfaceSignal> execute(String initialState) {
        return execute(initialState, new ModelInterfaceStandardSignals.EMPTY());
    }

    public Mono<ModelInterfaceSignal> execute(String initialState, ModelInterfaceSignal inputSignal) {
        final var init = Optional.ofNullable(states).map(x -> x.get(initialState))
                .orElseThrow(() -> new LlmGenerationConfigException(String.format("Cannot start execution; initial state '%s' not found", initialState)));

        
    }

//    private Mono<ModelInterfaceStateEmittedSignal> __executeStep(ModelInterfaceStateEmittedSignal lastStateOutput) {
//        // If we received a terminal signal then end the execution here
//        if (lastStateOutput.getSignal().isTerminal()) return Mono.empty();
//
//        // Attempt to find a matching rule, or otherwise fail execution
//        final var rule = rules.find(lastStateOutput);
//        if (rule.isEmpty()) {
//            return Mono.just(new ModelInterfaceStateEmittedSignal(lastStateOutput.getState(),
//                    new ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE(lastStateOutput.getState().getId(), lastStateOutput.getSignal().getId())));
//        }
//
//        // Evaluate the rule and execute the next state with this signal data
//        final var nextState = rule.get().getNextState();
//        return nextState.invoke(lastStateOutput.getSignal())
//                .map(outputSignal -> new ModelInterfaceStateEmittedSignal(nextState, outputSignal));
//    }

    private Mono<ModelInterfaceStateWithInputSignal> executeStep(ModelInterfaceStateWithInputSignal input) {
        // If this is a terminal state then end the execution here
        if (input.getState().isTerminal()) return Mono.empty();

        // Execute the action associated with this state
        return input.getState().invoke(input.getInputSignal())
                .map(outputSignal -> {
                    // Attempt to find a matching rule based on this state and the action output
                    return rules.find(input.getState(), outputSignal)

                            // Transition rule exists; move to the next step
                            .map(rule -> new ModelInterfaceStateWithInputSignal(rule.getNextState(), outputSignal))

                            // No matching transition rule.  Redirect to the built-in failure node
                            .orElseGet(() -> new ModelInterfaceStateWithInputSignal(defaultStateNoRule,
                                new ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE(input.getState().getId(), outputSignal.getId())));
                    });
    }


}
