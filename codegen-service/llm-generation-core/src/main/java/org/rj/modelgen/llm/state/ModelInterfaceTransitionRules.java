package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Optional;

public class ModelInterfaceTransitionRules {
    private final List<ModelInterfaceTransitionRule<? extends ModelInterfaceState>> rules;

    public ModelInterfaceTransitionRules(List<ModelInterfaceTransitionRule<? extends ModelInterfaceState>> rules) {
        this.rules = Optional.ofNullable(rules).map(List::copyOf).orElseGet(List::of);

        // Immutable rules, validate on initialization
        final var invalidRule = this.rules.stream().filter(x -> !x.isValid()).findAny();
        invalidRule.ifPresent(rule -> {
            throw new IllegalArgumentException(String.format("Cannot initialize with at least one invalid rule (%s)", rule));
        });
    }

    public List<ModelInterfaceTransitionRule<? extends ModelInterfaceState>> getRules() {
        return rules;
    }

    public Optional<ModelInterfaceTransitionRule<? extends ModelInterfaceState>>
    find(ModelInterfaceStateWithInputSignal<? extends ModelInterfaceState> signal) {
        if (signal == null) return Optional.empty();
        return find(signal.getState(), signal.getInputSignal());
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule<? extends ModelInterfaceState>>
    find(ModelInterfaceState currentState, ModelInterfaceSignal<? extends ModelInterfaceState> outputSignal) {
        return this.rules.stream()
                .filter(rule -> rule.matches(currentState, outputSignal))
                .findFirst();
    }
}
