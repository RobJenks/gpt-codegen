package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelInterfaceTransitionRules {
    private final List<ModelInterfaceTransitionRule> rules;

    public ModelInterfaceTransitionRules(List<ModelInterfaceTransitionRule> rules) {
        this.rules = new ArrayList<>(Optional.ofNullable(rules).orElseGet(List::of));

        // Immutable rules, validate on initialization
        final var invalidRule = this.rules.stream().filter(x -> !x.isValid()).findAny();
        invalidRule.ifPresent(rule -> {
            throw new IllegalArgumentException(String.format("Cannot initialize with at least one invalid rule (%s)", rule));
        });
    }

    public List<ModelInterfaceTransitionRule> getRules() {
        return rules;
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule> find(ModelInterfaceStateWithInputSignal signal) {
        if (signal == null) return Optional.empty();
        return find(signal.getState(), signal.getInputSignal().getId());
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule> find(ModelInterfaceState state, ModelInterfaceSignal outboundSignal) {
        if (state == null || outboundSignal == null) return Optional.empty();
        return find(state, outboundSignal.getId());
    }

    @JsonIgnore
    public Optional<ModelInterfaceTransitionRule>
    find(ModelInterfaceState currentState, String outputSignal) {
        return this.rules.stream()
                .filter(rule -> rule.matches(currentState, outputSignal))
                .findFirst();
    }
}
