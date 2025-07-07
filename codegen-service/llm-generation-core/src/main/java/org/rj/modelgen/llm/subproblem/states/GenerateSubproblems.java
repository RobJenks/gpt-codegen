package org.rj.modelgen.llm.subproblem.states;

import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class GenerateSubproblems extends SubproblemDecompositionBaseState {
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
    protected Mono<ModelInterfaceSignal> execute(ModelInterfaceSignal inputSignal) {
        // If problem decomposition is disabled we can just continue, since the full problem will already
        // be present in `request` for the rest of the model to work on
        if (!shouldDecomposeIntoSubproblems()) {
            LOG.info("Subproblem decomposition is disabled, continuing with full problem");
            return success("Subproblem decomposition is not enabled");
        }

        // If no subproblem ID is present then we have not yet run this process, and should decompose into subproblems now
        if (!getPayload().hasData(SubproblemDecompositionPayloadData.CurrentSubproblem)) {
            final var decompResult = triggerSubproblemDecomposition();
            if (decompResult.isErr()) return error(decompResult.getError());
        }

        // We have at least one subproblem to execute
        final int lastSubproblemId = getCurrentSubproblemId();
        final int subproblemCount = getSubproblemCount();

        if (lastSubproblemId >= (subproblemCount - 1)) {
            return error(String.format(
                    "Invalid subproblem state; trying to increment from previous subproblem %d with total subproblem count %d", lastSubproblemId, subproblemCount));
        }

        // Move to the next subproblem
        final var subproblemId = (lastSubproblemId + 1);
        getPayload().put(SubproblemDecompositionPayloadData.CurrentSubproblem, subproblemId);
        LOG.info("Starting processing of subproblem ID {} ({} of {})", subproblemId, subproblemId + 1, subproblemCount);

        // Get the request content for subproblem N and make it the active request which the rest of the model will work on
        final var requestKey = subproblemRequestContentKey(subproblemId);
        final String requestContent = getPayload().getOrThrow(requestKey, () -> new RuntimeException(
                String.format("No subproblem request content for subproblem %d (at %s)", subproblemId, requestKey)));

        getPayload().put(outputKey, requestContent);

        // Allow subclasses to perform their own preparation before starting work
        onStartingNewSubproblem(subproblemId, subproblemCount);

        return success("Subproblem generation successful");
    }

    private Result<Void, String> triggerSubproblemDecomposition() {
        final String problem = getPayload().getOrThrow(inputKey, () -> new RuntimeException("No problem data available for decomposition"));

        final var result = decomposeIntoSubproblems(problem);
        if (result.isErr()) return Result.Err(result.getError());

        // Store problem decomposition into payload for each iteration
        final List<String> subproblems = result.getValue();
        for (int i = 0; i < subproblems.size(); ++i) {
            getPayload().put(subproblemRequestContentKey(i), subproblems.get(i));
        }

        // Set 'last' subproblem to -1 so that the first execution increments to subproblem 0
        setCurrentSubproblemId(-1);
        setSubproblemCount(subproblems.size());

        LOG.info("Subproblem decomposition complete; generated {} subproblems", subproblems.size());
        return Result.Ok();
    }

    /**
     * Perform subproblem decomposition.  Returns a list containing the initial request content for each subproblem
     * @param problem       Problem to be decomposed and returned
     */
    protected abstract Result<List<String>, String> decomposeIntoSubproblems(String problem);

    /**
     * Triggered when we are about to prepare a new subproblem.  Can be overridden by subclasses to e.g. perform
     * cleanup of iteration N data in preparation for iteration N+1
     *
     * @param subproblemId          ID of the problem we are about to begin, from 0 to N-1
     * @param subproblemCount       Total number of subproblems (0 <= subproblemId < subproblemCount)
     */
    protected void onStartingNewSubproblem(int subproblemId, int subproblemCount) { }

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
