package org.rj.modelgen.llm.state;

public class ModelInterfaceStandardSignals {

    public static class FAIL_MAX_INVOCATIONS extends ModelInterfaceSignal {
        private static String ID = "FAIL_MAX_INVOCATIONS";
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
