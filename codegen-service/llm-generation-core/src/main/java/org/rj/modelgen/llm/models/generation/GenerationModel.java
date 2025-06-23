package org.rj.modelgen.llm.models.generation;

import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.state.GenerationComplete;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;

import java.util.List;

public abstract class GenerationModel extends ModelInterfaceStateMachine<GenerationComplete> {
    public GenerationModel(Class<? extends ModelInterfaceStateMachine<? extends GenerationComplete>> modelClass, ModelInterface modelInterface, List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
        super(modelClass, modelInterface, states, rules);
    }
}
