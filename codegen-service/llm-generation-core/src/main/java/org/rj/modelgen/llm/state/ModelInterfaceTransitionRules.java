package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Optional;

public class ModelInterfaceTransitionRules {
    private final List<ModelInterfaceTransitionRule> rules;

    public ModelInterfaceTransitionRules(List<ModelInterfaceTransitionRule> rules) {
        this.rules = Optional.ofNullable(rules).map(List::copyOf).orElseGet(List::of);

        // Immutable rules, validate on initialization
        final var invalidRule = this.rules.stream().filter(x -> !x.isValid()).findAny();
        invalidRule.ifPresent(rule -> {
            throw new IllegalArgumentException(String.format("Cannot initialize with at least one invalid rule (%s)", rule));
        });
    }

    public List<ModelInterfaceTransitionRule> getRules() {
        return rules;
    }


    // TODO - remove ?
    public Optional<ModelInterfaceTransitionRule> find(ModelInterfaceStateEmittedSignal signal) {
        if (signal == null) return Optional.empty();
        return find(signal.getState(), signal.getSignal());
    }

    public Optional<ModelInterfaceTransitionRule> find(ModelInterfaceStateWithInputSignal signal) {
        if (signal == null) return Optional.empty();
        return find(signal.getState(), signal.getInputSignal());
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule> find(ModelInterfaceState currentState, ModelInterfaceSignal outputSignal) {
        return this.rules.stream()
                .filter(rule -> rule.matches(currentState, outputSignal))
                .findFirst();
    }
}
