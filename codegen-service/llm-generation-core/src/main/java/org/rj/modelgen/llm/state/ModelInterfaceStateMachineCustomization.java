package org.rj.modelgen.llm.state;

import java.util.ArrayList;
import java.util.List;

public class ModelInterfaceStateMachineCustomization {
    private List<ModelInterfaceState> newStates;
    private List<String> removedStates;
    private List<ModelInterfaceTransitionRule.Reference> newRules;
    private List<ModelInterfaceTransitionRule.Reference> removedRules;

    public ModelInterfaceStateMachineCustomization() { }

    public ModelInterfaceStateMachineCustomization withNewStates(List<ModelInterfaceState> states) {
        if (newStates == null) newStates = new ArrayList<>();
        newStates.addAll(states);

        return this;
    }

    public ModelInterfaceStateMachineCustomization withRemovedStates(List<String> states) {
        if (removedStates == null) removedStates = new ArrayList<>();
        removedStates.addAll(states);

        return this;
    }

    public ModelInterfaceStateMachineCustomization withNewRules(List< ModelInterfaceTransitionRule.Reference> rules) {
        if (newRules == null) newRules = new ArrayList<>();
        newRules.addAll(rules);

        return this;
    }

    public ModelInterfaceStateMachineCustomization withRemovedRules(List< ModelInterfaceTransitionRule.Reference> rules) {
        if (removedRules == null) removedRules = new ArrayList<>();
        removedRules.addAll(rules);

        return this;
    }

    public List<ModelInterfaceState> getNewStates() {
        return newStates;
    }

    public List<String> getRemovedStates() {
        return removedStates;
    }

    public List<ModelInterfaceTransitionRule.Reference> getNewRules() {
        return newRules;
    }

    public List<ModelInterfaceTransitionRule.Reference> getRemovedRules() {
        return removedRules;
    }

    public ModelInterfaceStateMachineCustomization withNewState(ModelInterfaceState state) {
        return withNewStates(List.of(state));
    }

    public ModelInterfaceStateMachineCustomization withRemovedState(String state) {
        return withRemovedStates(List.of(state));
    }

    public ModelInterfaceStateMachineCustomization withNewRule(ModelInterfaceTransitionRule.Reference rule) {
        return withNewRules(List.of(rule));
    }

    public ModelInterfaceStateMachineCustomization withRemovedRule(ModelInterfaceTransitionRule.Reference rule) {
        return withRemovedRules(List.of(rule));
    }
}
