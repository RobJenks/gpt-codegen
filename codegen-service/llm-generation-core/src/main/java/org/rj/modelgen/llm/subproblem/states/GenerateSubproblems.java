package org.rj.modelgen.llm.subproblem.states;

import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class GenerateSubproblems extends ExecuteLogic implements SubproblemDecompositionState {
    private static final Logger LOG = LoggerFactory.getLogger(GenerateSubproblems.class);
    private String inputKey = StandardModelData.Request.toString();
    private String outputKey = StandardModelData.Request.toString();

    public GenerateSubproblems() {
        this(GenerateSubproblems.class);
    }

    public GenerateSubproblems(Class<? extends GenerateSubproblems> cls) {
        super(cls);
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        // If problem decomposition is disabled we can just continue, since the full problem will already
        // be present in `request` for the rest of the model to work on
        if (!shouldDecomposeIntoSubproblems()) return Mono.just(Result.Ok());

        // If no subproblem ID is present then we have not yet run this process, and should decompose into subproblems now
        if (!getPayload().hasData(SubproblemDecompositionPayloadData.CurrentSubproblem)) {
            final var decompResult = triggerSubproblemDecomposition();
            if (decompResult.isErr()) return Mono.just(Result.Err(decompResult.getError()));
        }

        // We have at least one subproblem to execute
        final Integer lastSubproblemId = getPayload().getOrThrow(SubproblemDecompositionPayloadData.CurrentSubproblem, () -> new RuntimeException("No subproblem ID in payload"));
        final Integer subproblemCount = getPayload().getOrThrow(SubproblemDecompositionPayloadData.SubproblemCount, () -> new RuntimeException("No subproblem count in payload"));

        if (lastSubproblemId >= (subproblemCount - 1)) {
            return Mono.just(Result.Err(String.format(
                    "Invalid subproblem state; trying to increment from previous subproblem %d with total subproblem count %d", lastSubproblemId, subproblemCount)));
        }

        // Move to the next subproblem
        final var subproblemId = (lastSubproblemId + 1);
        getPayload().put(SubproblemDecompositionPayloadData.CurrentSubproblem, subproblemId);

        // Get the request content for subproblem N and make it the active request which the rest of the model will work on
        final var requestKey = subproblemRequestContentKey(subproblemId);
        final var requestContent = getPayload().getOrThrow(requestKey, () -> new RuntimeException(
                String.format("No subproblem request content for subproblem %d (at %s)", subproblemId, requestKey)));

        getPayload().put(StandardModelData.Request, requestContent);

        return Mono.just(Result.Ok());
    }

    private Result<Void, String> triggerSubproblemDecomposition() {
        final var result = decomposeIntoSubproblems();
        if (result.isErr()) return Result.Err(result.getError());

        // Store problem decomposition into payload for each iteration
        final List<String> subproblems = result.getValue();
        for (int i = 0; i < subproblems.size(); ++i) {
            getPayload().put(subproblemRequestContentKey(i), subproblems.get(i));
        }

        LOG.info("Subproblem decomposition complete; generated {} subproblems", subproblems.size());
        return Result.Ok();
    }


    /**
     * If set, model will use `decomposeIntoSubproblems` to generate a set of subproblems and persist them
     * into the model payload.  Model will iterate until each subproblem is completed and then combine them
     */
    protected abstract boolean shouldDecomposeIntoSubproblems();

    /**
     * Perform subproblem decomposition.  Returns a list containing the initial request content for each subproblem
     */
    protected abstract Result<List<String>, String> decomposeIntoSubproblems();


    public GenerateSubproblems withInputKey(String inputKey) {
        this.inputKey = inputKey;
        return this;
    }

    public<T extends Enum<?>> GenerateSubproblems withInputKey(T inputKey) {
        return withInputKey(inputKey != null ? inputKey.toString() : null);
    }

    public String getInputKey() {
        return inputKey;
    }

    public GenerateSubproblems withOutputKey(String outputKey) {
        this.outputKey = outputKey;
        return this;
    }

    public<T extends Enum<?>> GenerateSubproblems withOutputKey(T outputKey) {
        return withOutputKey(outputKey != null ? outputKey.toString() : null);
    }

    public String getOutputKey() {
        return outputKey;
    }

}
