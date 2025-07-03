package org.rj.modelgen.llm.subproblem.states;

import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionSignals;
import org.rj.modelgen.llm.subproblem.data.SubproblemDetails;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

public abstract class CombineSubproblems extends SubproblemDecompositionBaseState {
    private static final Logger LOG = LoggerFactory.getLogger(CombineSubproblems.class);
    private String inputKey = MultiLevelModelStandardPayloadData.DetailLevelModel.toString();
    private String outputKey = MultiLevelModelStandardPayloadData.DetailLevelModel.toString();

    public CombineSubproblems() {
        super(CombineSubproblems.class);
    }

    public CombineSubproblems(Class<? extends CombineSubproblems> cls) {
        super(cls);
    }

    @Override
    protected Mono<ModelInterfaceSignal> execute(ModelInterfaceSignal inputSignal) {
        // If problem decomposition is disabled we can just continue, since the full generation result will already
        // be present in `ResponseContent` (and any explicit output key) for the rest of the model to work on
        if (!shouldDecomposeIntoSubproblems())
            return outboundSignal(SubproblemDecompositionSignals.SubproblemDecompositionCompleted, "Subproblem decomposition is not enabled").mono();

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

            return outboundSignal(SubproblemDecompositionSignals.ProcessNextSubproblem, String.format("Process next subproblem with ID " + nextSubproblemId)).mono();
        }

        // We have processed all subproblems - combine into a full solution
        final var combineResult = triggerSubproblemCombination();
        if (combineResult.isErr()) {
            return error("Subproblem recombination failed: " + combineResult.getError());
        }

        return outboundSignal(SubproblemDecompositionSignals.SubproblemDecompositionCompleted, "Subproblem recombination was successful").mono();
    }

    private Result<Void, String> triggerSubproblemCombination() {

        // Collect all subproblem requests and solutions for recombination
        final int subproblemCount = getSubproblemCount();
        final List<SubproblemDetails> subproblems = IntStream.range(0, subproblemCount)
                .mapToObj(i -> new SubproblemDetails(
                        getPayload().getOrThrow(subproblemRequestContentKey(i), () -> new RuntimeException("No subproblem request for ID " + i + " during recombination")),
                        getPayload().getOrThrow(subproblemResultContentKey(i), () -> new RuntimeException("No subproblem result for ID " + i + " during recombination"))
                ))
                .toList();

        // Delegate to the subclass for recombination logic.  Store the result or return failure
        final var result = combineSubproblems(subproblems);
        if (result.isErr()) {
            LOG.error("Failed when recombining {} subproblems following execution: {}", subproblemCount, result.getError());
            return Result.Err(result.getError());
        }

        // Successfully recombined all subproblems, so store as the regular model result expected by downstream stages
        getPayload().put(outputKey, result.getValue());
        return Result.Ok();
    }


    /**
     * If set, model will use `combineSubproblems` to combine the set of computed subproblem results and persist the
     * final result into the model payload
     */
    protected abstract boolean shouldDecomposeIntoSubproblems();

    /**
     * Perform subproblem recombination.  Accepts a list of subproblem details and recombines them into a single
     * problem solution
     * @param subproblems   The set of subproblems to be recombined
     */
    protected abstract Result<String, String> combineSubproblems(List<SubproblemDetails> subproblems);



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
