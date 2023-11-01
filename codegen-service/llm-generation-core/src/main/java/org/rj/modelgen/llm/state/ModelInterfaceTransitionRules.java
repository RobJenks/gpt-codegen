package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Optional;

public class ModelInterfaceTransitionRules {
    private final List<ModelInterfaceTransitionRule<? extends ModelInterfaceSignal>> rules;

    public ModelInterfaceTransitionRules(List<ModelInterfaceTransitionRule<? extends ModelInterfaceSignal>> rules) {
        this.rules = Optional.ofNullable(rules).map(List::copyOf).orElseGet(List::of);

        // Immutable rules, validate on initialization
        final var invalidRule = this.rules.stream().filter(x -> !x.isValid()).findAny();
        invalidRule.ifPresent(rule -> {
            throw new IllegalArgumentException(String.format("Cannot initialize with at least one invalid rule (%s)", rule));
        });
    }

    public List<ModelInterfaceTransitionRule<? extends ModelInterfaceSignal>> getRules() {
        return rules;
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule<? extends ModelInterfaceSignal>>
    find(ModelInterfaceStateWithInputSignal<? extends ModelInterfaceSignal> signal) {
        if (signal == null) return Optional.empty();
        return find(signal.getState(), signal.getInputSignal());
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule<? extends ModelInterfaceSignal>>
    find(ModelInterfaceState<? extends ModelInterfaceSignal> currentState, ModelInterfaceSignal outputSignal) {
        return this.rules.stream()
                .filter(rule -> rule.matches(currentState, outputSignal))
                .findFirst();
    }
}