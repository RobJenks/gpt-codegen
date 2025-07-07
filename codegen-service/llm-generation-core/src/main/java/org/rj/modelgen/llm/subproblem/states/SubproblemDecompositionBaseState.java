package org.rj.modelgen.llm.subproblem.states;

import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.states.common.BasicNode;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;

public abstract class SubproblemDecompositionBaseState extends BasicNode {
    private boolean subproblemDecompositionEnabled = true;

    public SubproblemDecompositionBaseState() {
        this(SubproblemDecompositionBaseState.class);
    }

    public SubproblemDecompositionBaseState(Class<? extends ModelInterfaceState> cls) {
        super(cls);
    }

    /**
     * If set, model will decompose a problem into subproblems, and later recombine the subproblem results into a full
     * solution in the model payload.  Model will iterate until each subproblem is completed and then combine them
     */
    public boolean shouldDecomposeIntoSubproblems() {
        return subproblemDecompositionEnabled;
    }

    public void setSubproblemDecompositionEnabled(boolean subproblemDecompositionEnabled) {
        this.subproblemDecompositionEnabled = subproblemDecompositionEnabled;
    }

    public SubproblemDecompositionBaseState withSubproblemDecompositionEnabled(boolean subproblemDecompositionEnabled) {
        setSubproblemDecompositionEnabled(subproblemDecompositionEnabled);
        return this;
    }

    protected String subproblemRequestContentKey(int subproblemId) {
        return String.format("%s-%d", SubproblemDecompositionPayloadData.SubproblemRequestContent, subproblemId);
    }

    protected String subproblemResultContentKey(int subproblemId) {
        return String.format("%s-%d", SubproblemDecompositionPayloadData.SubproblemResultContent, subproblemId);
    }

    protected int getCurrentSubproblemId() {
        return getPayload().<Integer, SubproblemDecompositionPayloadData>
                getOrThrow(SubproblemDecompositionPayloadData.CurrentSubproblem, () -> new RuntimeException("No subproblem ID in payload"));
    }

    protected void setCurrentSubproblemId(int id) {
        getPayload().put(SubproblemDecompositionPayloadData.CurrentSubproblem, id);
    }

    protected int getSubproblemCount() {
        return getPayload().<Integer, SubproblemDecompositionPayloadData>
                getOrThrow(SubproblemDecompositionPayloadData.SubproblemCount, () -> new RuntimeException("No subproblem count in payload"));
    }

    protected void setSubproblemCount(int count) {
        getPayload().put(SubproblemDecompositionPayloadData.SubproblemCount, count);
    }
}
