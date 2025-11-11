package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class PrepareModelForRendering extends ExecuteLogic {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareModelForRendering.class);

    public PrepareModelForRendering() {
        this(PrepareModelForRendering.class);
    }

    public PrepareModelForRendering(Class<? extends ExecuteLogic> cls) {
        super(cls);
    }

    // Remove any `null` nodes from the model.  Shouldn't happen but cleans up the model for future steps
    protected <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    void removeInvalidNullNodes(TModel model) {
        final var sanitized = model.getNodes().stream()
                .filter(Objects::nonNull)
                .toList();

        if (sanitized.size() != model.getNodes().size()) {
            LOG.info("Removing {} invalid null nodes from model", model.getNodes().size() - sanitized.size());
            model.setNodes(sanitized);
        }
    }

    // Identify orphaned subgraphs (which may more likely be individual nodes that became disconnected during preparation operations
    // above).  Remove them if they fall below a low threshold size.  Trigger a fatal generation error if they exceed the threshold
    // since it likely means we have generated a partitioned model and it needs to be fully regenerated
    protected <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    void identifyOrphanedSubgraphs(TModel model) {
        final int SUBGRAPH_SIZE_THRESHOLD = 3; // Threshold for pruning small subgraphs

        List<String> roots = ValidationUtils.identifyNumberOfRoots(model);

        // A correctly-formed model should have only one root (with |referrers| == 0)
        if (roots.size() == 1) {
            LOG.info("Validated that model is correctly-formed with a single root node ({})", roots.get(0));
            return;
        }

        // Model has no roots - it is cyclic and therefore invalid
        if (roots.isEmpty()) {
            final var error = "Generated model is cyclic with no roots; cannot continue with rendering";
            LOG.error(error);
            throw new RuntimeException(error);
        }

        // Model has multiple roots.  Determine the size of each subgraph.  If all bar one subgraph is below the threshold we
        // can prune them since they are minor orphaned nodes.  However if any are larger we either have a partitioned model or
        // multiple roots into the same large model
        final Map<String, List<String>> subgraphs = roots.stream()
                .collect(Collectors.toMap(Function.identity(), root -> calculateSubgraph(model, root)));

        final var aboveThreshold = subgraphs.entrySet().stream()
                .filter(e -> e.getValue().size() > SUBGRAPH_SIZE_THRESHOLD)
                .toList();

        // Fail if there are multiple subgraphs above the threshold (or if the model itself is small enough to be within the threshold)
        if (aboveThreshold.size() > 1 || model.getNodes().size() <= SUBGRAPH_SIZE_THRESHOLD) {
            final var error = String.format("Model contains multiple subgraphs.  More than one subgraph is above threshold size and so we cannot prune them " +
                            "to continue with rendering.  Model regeneration is required. Subgraph roots and sizes: [%s]",
                    subgraphs.entrySet().stream().map(e -> String.format("%s (%d)", e.getKey(), e.getValue().size())).collect(Collectors.joining(", ")));
            LOG.error(error);
            throw new RuntimeException(error);
        }

        // Multiple subgraphs, but all bar the main graph are below threshold so we can prune them.  There are no references to the subgraphs
        // from outside the subgraphs (by definition) so we can safely remove them
        LOG.warn("Model contains multiple subgraphs.  Extra subgraphs are below threshold size and will be removed to allow rendering. " +
                "Subgraph roots and sizes: [{}]", subgraphs.entrySet().stream().map(e -> String.format("%s (%d)", e.getKey(), e.getValue().size()))
                .collect(Collectors.joining(", ")));

        final var toRemove = subgraphs.entrySet().stream()
                .filter(e -> e.getValue().size() <= SUBGRAPH_SIZE_THRESHOLD)
                .toList();
        if (toRemove.size() != subgraphs.size() - 1) {
            LOG.warn("Expected to remove {} subgraphs but found {} to remove; skipping pruning logic", subgraphs.size() - 1, toRemove.size());
            return;
        }

        for (final var remove : toRemove) {
            LOG.info("Removing orphaned subgraph from model [{}]", String.join(", ", remove.getValue()));
            removeSubgraph(model, remove.getValue());
        }
    }

    protected <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    List<String> calculateSubgraph(TModel model, String root) {
        final Set<String> visited = new HashSet<>();
        final List<String> queue = new ArrayList<>();
        queue.add(root);

        var nodeMap = model.getNodes().stream().collect(Collectors.toMap(GraphNode::getId, Function.identity()));

        while (!queue.isEmpty()) {
            final String nodeId = queue.remove(queue.size() - 1);
            if (visited.contains(nodeId)) continue;

            visited.add(nodeId);

            final var node = nodeMap.get(nodeId);
            if (node == null) {
                LOG.warn("Encountered unknown node id '{}' specified as target while calculating subgraph size; skipping", nodeId);
                continue;
            }

            // Follow all connections
            node.getConnectedTo().forEach(conn -> queue.add(conn.getTargetNode()));
        }

        return visited.stream().toList();
    }

    private <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    void removeSubgraph(TModel model, List<String> nodes) {
        // Simply remove the subgraph nodes.  There can be no connections into or out of the subgraph by definition
        final Set<String> remove = Optional.ofNullable(nodes).map(HashSet::new).orElseGet(HashSet::new);
        model.getNodes().removeIf(n -> remove.contains(n.getId())); // I need to make ElementConnections to contains node ids not names
    }

    /**
     * Must be implemented by subclasses.  Execute custom logic and return a result which will
     * determine the outbound signal sent back to the model
     *
     * @return      Result of the execution
     */
    protected abstract Mono<Result<Void, String>> executeLogic();
}
