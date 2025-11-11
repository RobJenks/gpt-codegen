package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;

public abstract class ExecuteLogic extends ModelInterfaceState {

    private static final Logger LOG = LoggerFactory.getLogger(ExecuteLogic.class);

    protected String inputKeyOverride;

    public ExecuteLogic() {
        this(ExecuteLogic.class);
    }

    public ExecuteLogic(Class<? extends ExecuteLogic> cls) {
        super(cls);
    }

    @Override
    public String getDescription() {
        return "Executes custom logic";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        return executeLogic()
                .flatMap(result -> result
                        .map(__ -> outboundSignal(getSuccessSignalId()).mono())
                        .orElse(this::error));
    }

    protected <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    Mono<Result<Void, String>> execute(TModel model, List<Runnable> operations) {
        // Apply all operations; catch and propagate any unhandled exceptions
        for (final var operation : operations) {
            try {
                operation.run();
            } catch (Throwable t) {
                final var errorMessage = String.format("Error during model preparation operation: %s", t.getMessage());
                LOG.error(errorMessage, t);
                return Mono.just(Result.Err(errorMessage));
            }
        }

        final var saveResult = saveModelData(model);
        if (saveResult.isErr()) {
            return Mono.just(Result.Err("Failed to save model during render preparation: " + saveResult.getError()));
        }

        return Mono.just(Result.Ok());
    }

    private String getModelKey() {
        return Optional.ofNullable(inputKeyOverride).orElse(MultiLevelModelStandardPayloadData.DetailLevelModel.toString());
    }

    private <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    Result<Void, String> saveModelData(TModel model) {
        if (model == null) return Result.Err("Cannot save null model");

        final var serialized = Util.trySerialize(model);
        if (serialized.isErr()) return Result.Err("Cannot serialize model: " + serialized.getError().getMessage());

        getPayload().put(getModelKey(), serialized.getValue());
        return Result.Ok();
    }

    /**
     * Must be implemented by subclasses.  Execute custom logic and return a result which will
     * determine the outbound signal sent back to the model
     *
     * @return      Result of the execution
     */
    protected abstract Mono<Result<Void, String>> executeLogic();

    /**
     * Result of executing the logic node, which will determine the outbound signal sent back to the model
     */
    protected enum ExecuteLogicResult {
        SUCCESS,
        FAILURE
    }
}
