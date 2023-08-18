package org.rj.modelgen.llm.state;

import java.util.Map;

public class ModelInterfaceStateMachine {
    private final Map<String, ModelInterfaceState> states;
    private final ModelInterfaceTransitionRules rules;

    public ModelInterfaceStateMachine(Map<String, ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
        this.states = states;
        this.rules = rules;
    }

    

}
