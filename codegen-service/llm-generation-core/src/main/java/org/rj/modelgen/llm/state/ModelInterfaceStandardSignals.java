package org.rj.modelgen.llm.state;

public class ModelInterfaceStandardSignals {

    /* Empty signal, e.g. if triggering a state with no data required  */
    public static class EMPTY extends ModelInterfaceSignal<ModelInterfaceState> {
        public static String ID = "EMPTY";

        public EMPTY() { super(ID); }

        @Override
        public String getDescription() {
            return "Empty signal";
        }
    }

    /* Failure due to no matching transition rule from the current state */
    public static class FAIL_NO_MATCHING_TRANSITION_RULE extends ModelInterfaceSignal<ModelInterfaceState> {
        public static String ID = "FAIL_NO_MATCHING_TRANSITION_RULE";
        private final String state;
        private final String outputSignal;

        public FAIL_NO_MATCHING_TRANSITION_RULE(String state, String outputSignal) {
            super(ID);
            this.state = state;
            this.outputSignal = outputSignal;
        }

        @Override
        public String getDescription() {
            return String.format("No transition rule for state '%s' with output signal '%s', cannot continue execution", state, outputSignal);
        }
    };

    /* Failure due to max invocations of a single state */
    public static class FAIL_MAX_INVOCATIONS extends ModelInterfaceSignal<ModelInterfaceState> {
        public static String ID = "FAIL_MAX_INVOCATIONS";
        private final String atState;
        private final int invocations;

        public FAIL_MAX_INVOCATIONS(String atState, int invocations) {
            super(ID);
            this.atState = atState;
            this.invocations = invocations;
        }

        @Override
        public String getDescription() {
            return String.format("Exceeded execution limit of state '%s' at %d invocations", atState, invocations);
        }
    };

}
