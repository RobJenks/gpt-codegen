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
        return Result.Ok(List.of(problem));
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
        ));
    }
}
