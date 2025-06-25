package org.rj.modelgen.llm.models.generation;

import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public abstract class GenerationModel<TResult extends GenerationResult> extends ModelInterfaceStateMachine {
    public GenerationModel(Class<? extends ModelInterfaceStateMachine> modelClass, ModelInterface modelInterface, List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
        super(modelClass, modelInterface, states, rules);
    }

    /**
     * Implemented by subclasses to trigger execution of a specific model type
     *
     * @param sessionId     Current session
     * @param request       Input prompt
     * @param data          Initial input data to be passed into the model in the input signal
     * @return              Model execution result
     */
    public abstract Mono<TResult> executeModel(String sessionId, String request, Map<String, Object> data);
}
