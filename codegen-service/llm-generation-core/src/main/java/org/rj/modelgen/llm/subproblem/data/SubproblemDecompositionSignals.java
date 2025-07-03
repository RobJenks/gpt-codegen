package org.rj.modelgen.llm.subproblem.data;

public enum SubproblemDecompositionSignals {
    ProcessNextSubproblem,
    SubproblemDecompositionCompleted;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
