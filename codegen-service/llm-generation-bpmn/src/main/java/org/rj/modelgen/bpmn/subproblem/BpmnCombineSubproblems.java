package org.rj.modelgen.bpmn.subproblem;

import org.rj.modelgen.llm.subproblem.data.SubproblemDetails;
import org.rj.modelgen.llm.subproblem.states.CombineSubproblems;
import org.rj.modelgen.llm.util.Result;

import java.util.List;
import java.util.stream.Collectors;

public class BpmnCombineSubproblems extends CombineSubproblems {
    public BpmnCombineSubproblems() {
        super(BpmnCombineSubproblems.class);
    }

    @Override
    protected Result<String, String> combineSubproblems(List<SubproblemDetails> subproblems) {
        if (subproblems == null) return Result.Err("No valid subproblems to recombine");

        return Result.Ok(subproblems.stream()
                .map(SubproblemDetails::result)
                .collect(Collectors.joining("\n")));
    }
}
