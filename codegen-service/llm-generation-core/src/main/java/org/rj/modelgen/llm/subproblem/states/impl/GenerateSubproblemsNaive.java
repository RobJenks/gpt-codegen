package org.rj.modelgen.llm.subproblem.states.impl;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.subproblem.states.GenerateSubproblems;
import org.rj.modelgen.llm.util.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example concrete subclass of `GenerateProblems`.  Simply divides the problem into N parts.  Not suitable
 * for much except testing
 */
public class GenerateSubproblemsNaive extends GenerateSubproblems {
    private static final int SUBPROBLEM_COUNT = 4;

    public GenerateSubproblemsNaive() {
        super(GenerateSubproblemsNaive.class);
    }

    @Override
    protected Result<List<String>, String> decomposeIntoSubproblems(String problem) {
        if (problem == null) return Result.Err("No valid problem to decompose");

        final var lines = Arrays.asList(StringUtils.split(problem, '\n'));
        final int lineCount = lines.size();
        final int baseSubsetSize = (int)Math.floor((float)lineCount / SUBPROBLEM_COUNT);

        // Each subproblem contains equal base count, and remainder (< subproblem count) is distributed across subproblems
        final int baseTotalAmount = (int)baseSubsetSize * SUBPROBLEM_COUNT;
        final int remainder = lineCount - baseTotalAmount;

        final List<String> subproblems = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < SUBPROBLEM_COUNT; ++i) {
            final var count = baseSubsetSize + (i < remainder ? 1 : 0);
            final String subproblem = (count == 0) ? "" :
                    String.join("\n", lines.subList(offset, offset + count));

            subproblems.add(subproblem);
            offset += count;
        }

        return Result.Ok(subproblems);
    }
}
