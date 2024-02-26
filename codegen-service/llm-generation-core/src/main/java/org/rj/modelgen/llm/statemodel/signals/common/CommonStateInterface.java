package org.rj.modelgen.llm.statemodel.signals.common;

public interface CommonStateInterface {

    /**
     * Provides the signal ID to be emitted when this state completes successfully
     * @return          ID of the signal to be emitted when this state completes successfully
     */
    String getSuccessSignalId();

}
