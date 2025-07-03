package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public abstract class BasicNode extends ModelInterfaceState {

    public BasicNode() {
        this(BasicNode.class);
    }

    public BasicNode(Class<? extends ModelInterfaceState> cls) {
        super(cls);
    }

    @Override
    public String getDescription() {
        return "Basic node";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        // Basic node simply delegates all calls to the subclass
        return execute(inputSignal);
    }

    /**
     * Implemented by sublasses.  BasicNode simply delegates the invoke call to the subclass and takes
     * no other actions.
     * @param inputSignal       Signal triggering this node
     * @return                  Outbound signal to determine next model state
     */
    protected abstract Mono<ModelInterfaceSignal> execute(ModelInterfaceSignal inputSignal);
}
