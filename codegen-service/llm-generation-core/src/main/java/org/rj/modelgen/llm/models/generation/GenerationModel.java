package org.rj.modelgen.llm.models.generation;

import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;

import java.util.List;

public abstract class GenerationModel extends ModelInterfaceStateMachine {
    public GenerationModel(Class<? extends ModelInterfaceStateMachine> modelClass, ModelInterface modelInterface, List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
        super(modelClass, modelInterface, states, rules);
    }

    public abstract String getStringifiedResult();
}
