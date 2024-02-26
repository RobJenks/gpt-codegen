package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.statemodel.signals.common.StandardErrorSignals;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;

public class ModelInterfaceStandardSignals {

    /* Empty signal, e.g. if triggering a state with no data required  */
    public static class EMPTY extends ModelInterfaceSignal {
        public EMPTY() { super(StandardSignals.EMPTY, "Empty signal"); }
    }

    /* Failure due to no matching transition rule from the current state */
    public static class FAIL_NO_MATCHING_TRANSITION_RULE extends ModelInterfaceSignal {
        private final String state;
        private final String outputSignal;

        public FAIL_NO_MATCHING_TRANSITION_RULE(String state, String outputSignal) {
            super(StandardErrorSignals.NO_TRANSITION_RULE, String.format("No transition rule for state '%s' with output signal '%s', cannot continue execution", state, outputSignal));
            this.state = state;
            this.outputSignal = outputSignal;
        }

        public String getState() {
            return state;
        }

        public String getOutputSignal() {
            return outputSignal;
        }
    };

    /* Failure due to max invocations of a single state */
    public static class FAIL_MAX_INVOCATIONS extends ModelInterfaceSignal {
        private final String state;
        private final int invocations;

        public FAIL_MAX_INVOCATIONS(String state, int invocations) {
            super(StandardErrorSignals.FAILED_MAX_INVOCATIONS, String.format("Exceeded execution limit of state '%s' at %d invocations", state, invocations));
            this.state = state;
            this.invocations = invocations;
        }

        public String getState() {
            return state;
        }

        public int getInvocations() {
            return invocations;
        }
    };

    /* Generic error signal; will be caught by a global error handler if not explicitly handled in transition rules */
    public static class GENERAL_ERROR extends ModelInterfaceSignal {
        private final String state;
        private final String error;

        public GENERAL_ERROR(String state, String error) {
            super(StandardErrorSignals.GENERAL_ERROR, String.format("Error encountered at state '%s': %s", state, error));
            this.state = state;
            this.error = error;
        }

        public String getState() {
            return state;
        }

        public String getError() {
            return error;
        }
    };

}
