package org.rj.modelgen.bpmn.subproblem;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.subproblem.states.GenerateSubproblems;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BpmnGenerateSubproblems extends GenerateSubproblems {
    private static final Logger LOG = LoggerFactory.getLogger(BpmnGenerateSubproblems.class);
    private static final int SUBPROBLEM_COUNT = 4;

    public BpmnGenerateSubproblems() {
        super(BpmnGenerateSubproblems.class);
    }

    @Override
    protected Result<List<String>, String> decomposeIntoSubproblems(String problem) {
        if (StringUtils.isEmpty(problem)) return Result.Err("No valid problem to decompose");

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
            if (count == 0) break;

            final String subproblem = String.join("\n", lines.subList(offset, offset + count));

            subproblems.add(subproblem);
            offset += count;
        }

        return Result.Ok(subproblems);
    }

    @Override
    protected void onStartingNewSubproblem(int subproblemId, int subproblemCount) {
        // Clear any intermediate payload data generated during the last subproblem.  This could
        // otherwise interfere with generation of the next subproblem result
        getPayload().removeAll(List.of(
                StandardModelData.ResponseContent.toString(),
                StandardModelData.ModelResponse.toString(),
                StandardModelData.ValidationMessages.toString(),
                MultiLevelModelStandardPayloadData.HighLevelModel.toString(),
                MultiLevelModelStandardPayloadData.DetailLevelModel.toString()
                //BpmnPromptPlaceholders.GLOBAL_VARIABLES_USED_IN_HL_MODEL.getValue()
        ));
    }
}
