package org.rj.modelgen.llm.state;

import reactor.core.publisher.Mono;

public class ModelInterfaceStandardStates {

    /* Built-in state where execution is routed when no matching transition rule exists */
    public static class NO_TRANSITION_RULE extends ModelInterfaceSpecializedState<ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE> {
        public NO_TRANSITION_RULE() {
            super(NO_TRANSITION_RULE.class, ModelInterfaceStateType.TERMINAL_FAILURE);
            setLastError(getDescription());
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

    /* Built-in state where execution is routed when maximum invocation count for a state has been exceeded */
    public static class EXCEEDED_MAX_INVOCATIONS extends ModelInterfaceSpecializedState<ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS> {
        public EXCEEDED_MAX_INVOCATIONS() {
            super(EXCEEDED_MAX_INVOCATIONS.class, ModelInterfaceStateType.TERMINAL_FAILURE);
            setLastError(getDescription());
        }

        @Override
        public String getDescription() {
            return "Model exceeded maximum allowed invocations of the previous state";
        }

        @Override
        protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
            // Never invoked
            return Mono.empty();
        }
    }

    /* Built-in state which catches any generic errors that are not explicitly handled by model transition rules */
    public static class FAILED_WITH_ERROR extends ModelInterfaceSpecializedState<ModelInterfaceStandardSignals.GENERAL_ERROR> {
        private String failedAtState;

        public FAILED_WITH_ERROR() {
            super(FAILED_WITH_ERROR.class, ModelInterfaceStateType.TERMINAL_FAILURE);
        }

        @Override
        public String getDescription() {
            return String.format("Model failed with an error (%s)", getLastError());
        }

        @Override
        protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
            final var input = asExpectedInputSignal(inputSignal);

            setLastError(input.getError());
            this.failedAtState = input.getState();

            return Mono.empty();
        }
    }
}
