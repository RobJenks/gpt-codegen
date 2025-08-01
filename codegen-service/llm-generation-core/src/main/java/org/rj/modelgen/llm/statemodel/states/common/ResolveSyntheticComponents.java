package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.synthetic.SyntheticNode;
import org.rj.modelgen.llm.synthetic.SyntheticNodeConfig;
import org.rj.modelgen.llm.synthetic.SyntheticNodeResolver;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.StringSerializable;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ResolveSyntheticComponents<TNodeId,
                                                 TConnection extends GraphConnection<TNodeId>,
                                                 TNode extends GraphNode<TNodeId, TConnection>,
                                                 TModel extends IntermediateGraphModel<TNodeId, TConnection, TNode>,
                                                 TSyntheticNodeTypeId extends StringSerializable,
                                                 TSyntheticNode extends SyntheticNode<TNodeId, TConnection, TNode, TModel>,
                                                 TConfig extends SyntheticNodeConfig<TNodeId, TConnection, TNode, TModel, TSyntheticNodeTypeId, TSyntheticNode, ?>,
                                                 TResolver extends SyntheticNodeResolver<TNodeId, TConnection, TNode, TModel, TSyntheticNodeTypeId, TSyntheticNode, TConfig>> extends ExecuteLogic {

    private final TResolver resolver;

    public ResolveSyntheticComponents(TResolver resolver) {
        this(ResolveSyntheticComponents.class, resolver);
    }

    public ResolveSyntheticComponents(Class<? extends ResolveSyntheticComponents> cls, TResolver resolver) {
        super(cls);
        this.resolver = resolver;
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        final var modelData = getModelData();
        if (modelData.isErr()) return Mono.just(Result.Err(modelData.getError()));

        final var model = modelData.getValue();

        final var resolvedModel = Optional.ofNullable(model)
                .map(this::resolve)
                .orElseThrow(() -> new RuntimeException("Internal error while resolving synthetic nodes"));

        if (resolvedModel.isErr()) {
            return Mono.just(Result.Err("Error while resolving synthetic nodes: %s".formatted(resolvedModel.getError())));
        }

        final var updateResult = setModelData(resolvedModel.getValue());
        if (updateResult.isErr()) {
            return Mono.just(Result.Err("Failed to update execution model following synthetic component resolution: %s".formatted(updateResult.getError())));
        }

        return Mono.just(Result.Ok());
    }

    /**
     * Resolves all resolvable node types by default.  Can be overridden by subclasses if necessary, for example in
     * order to resolve only certain types, or enforce a certain resolution order
     *
     * @param model     Current model state
     * @return          Modified model
     */
    protected Result<TModel, String> resolve(TModel model) {
        return Result.Ok(resolveAll(model));
    }


    protected final TModel resolveAll(TModel model) {
        resolver.getResolvableTypes()
                .forEach(type -> resolveAllOfType(type, model));

        return model;
    }

    protected final TModel resolveAllOfType(TSyntheticNodeTypeId type, TModel model) {
        return resolveAllOfType(Optional.ofNullable(type).map(StringSerializable::toString).orElse(null), model);
    }

    protected final TModel resolveAllOfType(String type, TModel model) {
        forEachNodeOfType(model, type, node -> resolver.resolve(type, node, model));
        return model;
    }

    private void forEachNodeOfType(TModel model, TSyntheticNodeTypeId type, Consumer<TNode> action) {
        forEachNodeOfType(model, type.toString(), action);
    }

    private void forEachNodeOfType(TModel model, String type, Consumer<TNode> action) {
        // Collect node IDs and then call one by one, to avoid concurrent access issues if a synthetic node modifies
        // the model or its node collection
        final var nodeIds = model.getAllNodesRecursive()
                .filter(node -> isOfType(node, type))
                .map(GraphNode::getId)
                .toList();

        for (final var id : nodeIds) {
            if (id == null) continue;

            final var node = model.getNodes().stream()
                    .filter(n -> id.equals(n.getId()))
                    .findFirst().orElse(null);

            if (node == null) continue;
            if (!isOfType(node, type)) continue;    // A previous synthetic node may have changed our type since we were added

            action.accept(node);
        }
    }

    /**
     * Implemented by subclasses. Get the current, typed execution model
     */
    protected abstract Result<TModel, String> getModelData();

    /**
     * Implemented by subclasses.  Update the current state of the execution model, after all nodes have been resolved
     */
    protected abstract Result<Void, String> setModelData(TModel model);

    /**
     * Implemented by subclasses.  Predicate to determine whether a node is of the given type
     */
    protected abstract boolean isOfType(TNode node, String type);

}
