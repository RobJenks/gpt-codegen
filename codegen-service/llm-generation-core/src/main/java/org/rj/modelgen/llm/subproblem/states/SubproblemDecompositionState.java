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
    ModelInterfacePayload getModelPayload();

    default int getCurrentSubproblemId() {
        getPayload().getOrThrow(SubproblemDecompositionPayloadData.CurrentSubproblem, () -> new RuntimeException("No subproblem ID in payload"));
    }

}
