package org.rj.modelgen.llm.state;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ModelInterfaceStateMachineCustomization {
    private List<ModelInterfaceState> newStates;
    private List<String> removedStates;
    private List<ModelInterfaceTransitionRule.Reference> newRules;
    private List<ModelInterfaceTransitionRule.Reference> removedRules;
    private List<Pair<ModelInterfaceState, String>> insertStateAfter;

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

    /**
     * Insert a new state after the given state.  Reconnects any "SUCCESS" connections from `insertAfter` to subsequent nodes
     * in order to insert this new state.  E.g. if current 'success' connections are A->B->C and this method is called
     * for state X, insertAfter=B, the new 'success' chain becomes A->B->X->C.  Non-success connections are not modified
     *
     * @param state             State to be inserted
     * @param insertAfter       ID of the state to insert this state after
     *
     * @return                  Reference to the customization
     */
    public ModelInterfaceStateMachineCustomization withNewStateInsertedAfter(ModelInterfaceState state, String insertAfter) {
        if (state == null || StringUtils.isEmpty(insertAfter)) return this;

        if (insertStateAfter == null) insertStateAfter = new ArrayList<>();
        insertStateAfter.add(ImmutablePair.of(state, insertAfter));

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

    public List<Pair<ModelInterfaceState, String>> getInsertStateAfter() {
        return insertStateAfter;
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
