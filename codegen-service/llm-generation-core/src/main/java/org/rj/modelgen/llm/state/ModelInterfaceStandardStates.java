package org.rj.modelgen.llm.state;

import reactor.core.publisher.Mono;

public class ModelInterfaceStandardStates {

    /* Built-in state where is execution is routed when no matching transition rule exists */
    public static class NO_TRANSITION_RULE extends ModelInterfaceState {
        private static final String ID = "NO_TRANSITION_RULE";
        public NO_TRANSITION_RULE() {
            super(ID, ModelInterfaceStateType.TERMINAL_FAILURE);
        }

        @Override
        public String getDescription() {
            return "Model failed due to no matching transition rule for the current state & signal";
        }

        @Override
        protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
            // Never invoked
            return Mono.empty();
        }
    }
}
