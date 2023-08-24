package org.rj.modelgen.llm.state;

public class ModelInterfaceStandardSignals {

    /* Empty signal, e.g. if triggering a state with no data required  */
    public static class EMPTY extends ModelInterfaceSignal {
        public EMPTY() { super(EMPTY.class); }

        @Override
        public String getDescription() {
            return "Empty signal";
        }
    }

    /* Failure due to no matching transition rule from the current state */
    public static class FAIL_NO_MATCHING_TRANSITION_RULE extends ModelInterfaceSignal {
        private final String state;
        private final String outputSignal;

        public FAIL_NO_MATCHING_TRANSITION_RULE(String state, String outputSignal) {
            super(FAIL_NO_MATCHING_TRANSITION_RULE.class);
            this.state = state;
            this.outputSignal = outputSignal;
        }

        @Override
        public String getDescription() {
            return String.format("No transition rule for state '%s' with output signal '%s', cannot continue execution", state, outputSignal);
        }
    };

    /* Failure due to max invocations of a single state */
    public static class FAIL_MAX_INVOCATIONS extends ModelInterfaceSignal {
        private final String atState;
        private final int invocations;

        public FAIL_MAX_INVOCATIONS(String atState, int invocations) {
            super(FAIL_MAX_INVOCATIONS.class);
            this.atState = atState;
            this.invocations = invocations;
        }

        @Override
        public String getDescription() {
            return String.format("Exceeded execution limit of state '%s' at %d invocations", atState, invocations);
        }
    };

}
