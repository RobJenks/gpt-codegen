package org.rj.modelgen.llm.subproblem.states;

import org.rj.modelgen.llm.models.generation.multilevel.MultiLevelGenerationModelStates;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionSignals;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public abstract class CombineSubproblems extends ExecuteLogic implements SubproblemDecompositionState {
    private static final Logger LOG = LoggerFactory.getLogger(CombineSubproblems.class);
    private String inputKey = MultiLevelGenerationModelStates.ExecuteDetailLevel.toString();
    private String outputKey = MultiLevelGenerationModelStates.ExecuteDetailLevel.toString();

    public CombineSubproblems() {
        super(CombineSubproblems.class);
    }

    public CombineSubproblems(Class<? extends CombineSubproblems> cls) {
        super(cls);
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        // If problem decomposition is disabled we can just continue, since the full generation result will already
        // be present in `ResponseContent` (and any explicit output key) for the rest of the model to work on
        if (!shouldDecomposeIntoSubproblems()) return Mono.just(Result.Ok());

        // We have a decomposition into subproblems.  Process the subproblem we have just completed
        final Integer currentSubproblemId = getPayload().getOrThrow(SubproblemDecompositionPayloadData.CurrentSubproblem, () -> new RuntimeException("No subproblem ID in payload"));
        final String currentSubproblemResult = getPayload().getOrThrow(inputKey, () -> new RuntimeException("No current result present while processing subproblem " + currentSubproblemId));
        getPayload().put(subproblemResultContentKey(currentSubproblemId), currentSubproblemResult);

        // Do we have any more subproblems to solve?
        final Integer subproblemCount = getPayload().getOrThrow(SubproblemDecompositionPayloadData.SubproblemCount, () -> new RuntimeException("No subproblem count in payload"));
        if (currentSubproblemId < (subproblemCount - 1)) {
            // There are more subproblems to process so run another pass through the model
            final Integer nextSubproblemId = currentSubproblemId + 1;
            getPayload().put(SubproblemDecompositionPayloadData.CurrentSubproblem, nextSubproblemId);

            return outboundSignal(SubproblemDecompositionSignals.ProcessNextSubproblem, String.format("Process next subproblem with ID " + nextSubproblemId);
        }

        // We have processed all subproblems - combine into a full solution
        final var combineResult = triggerSubproblemCombination();

    }

    private Result<Void, String> triggerSubproblemCombination() {
        // Collect all subproblem requests and solutions, and pass them to the subclass to recombine

    }


    protected abstract boolean shouldDecomposeIntoSubproblems();

    protected abstract Result<String, String> combineSubproblems();



    public CombineSubproblems withInputKey(String inputKey) {
        this.inputKey = inputKey;
        return this;
    }

    public<T extends Enum<?>> CombineSubproblems withInputKey(T inputKey) {
        return withInputKey(inputKey != null ? inputKey.toString() : null);
    }

    public String getInputKey() {
        return inputKey;
    }

    public CombineSubproblems withOutputKey(String outputKey) {
        this.outputKey = outputKey;
        return this;
    }

    public<T extends Enum<?>> CombineSubproblems withOutputKey(T outputKey) {
        return withOutputKey(outputKey != null ? outputKey.toString() : null);
    }

    public String getOutputKey() {
        return outputKey;
    }
}
