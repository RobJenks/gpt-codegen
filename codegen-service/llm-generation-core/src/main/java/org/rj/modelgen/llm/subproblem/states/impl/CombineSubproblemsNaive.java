package org.rj.modelgen.llm.subproblem.states.impl;

import org.rj.modelgen.llm.subproblem.data.SubproblemDetails;
import org.rj.modelgen.llm.subproblem.states.CombineSubproblems;
import org.rj.modelgen.llm.util.Result;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Example concrete subclass of `CombineProblems`.  Naive subproblem decomposition simply divides the problem
 * into N parts.  The combine stage is therefore just concatenation.  Not suitable for much except testing
 */
public class CombineSubproblemsNaive extends CombineSubproblems {
    public CombineSubproblemsNaive() {
        super(CombineSubproblemsNaive.class);
    }

    @Override
    protected Result<String, String> combineSubproblems(List<SubproblemDetails> subproblems) {
        if (subproblems == null) return Result.Err("No valid subproblems to recombine");

        return Result.Ok(subproblems.stream()
                .map(SubproblemDetails::result)
                .collect(Collectors.joining("\n")));
    }
}
