package org.rj.modelgen.llm.subproblem.states;

import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;

public interface SubproblemDecompositionState {

    default String subproblemRequestContentKey(int subproblemId) {
        return String.format("%s-%d", SubproblemDecompositionPayloadData.SubproblemRequestContent, subproblemId);
    }

    default String subproblemResultContentKey(int subproblemId) {
        return String.format("%s-%d", SubproblemDecompositionPayloadData.SubproblemResultContent, subproblemId);
    }

    // Implemented by subclasses to expose the model payload
    ModelInterfacePayload getProblemDecompositionPayloadData();

    default int getCurrentSubproblemId() {
        return getProblemDecompositionPayloadData().<Integer, SubproblemDecompositionPayloadData>
                getOrThrow(SubproblemDecompositionPayloadData.CurrentSubproblem, () -> new RuntimeException("No subproblem ID in payload"));
    }

    default void setCurrentSubproblemId(int id) {
        getProblemDecompositionPayloadData().put(SubproblemDecompositionPayloadData.CurrentSubproblem, id);
    }

    default int getSubproblemCount() {
        return getProblemDecompositionPayloadData().<Integer, SubproblemDecompositionPayloadData>
                getOrThrow(SubproblemDecompositionPayloadData.SubproblemCount, () -> new RuntimeException("No subproblem count in payload"));
    }

    default void setSubproblemCount(int count) {
        getProblemDecompositionPayloadData().put(SubproblemDecompositionPayloadData.SubproblemCount, count);
    }

}
