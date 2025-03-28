package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.*;

public class SyntheticNodeConfig<TNodeId,
                                          TConnection extends GraphConnection<TNodeId>,
                                          TNode extends GraphNode<TNodeId, TConnection>,
                                          TModel extends IntermediateGraphModel<TNodeId, TConnection, TNode>,
                                          TSyntheticNodeTypeId extends StringSerializable,
                                          TSyntheticNode extends SyntheticNode<TNodeId, TConnection, TNode, TModel>> {

    private final Map<String, Class<? extends TSyntheticNode>> syntheticNodes;

    public SyntheticNodeConfig() {
        this(new HashMap<>());
    }

    public SyntheticNodeConfig(Map<String, Class<? extends TSyntheticNode>> syntheticNodes) {
        this.syntheticNodes = syntheticNodes;
    }

    public Optional<Class<? extends TSyntheticNode>> getNode(TSyntheticNodeTypeId id) {
        return Optional.ofNullable(id).map(StringSerializable::toString)
                .map(syntheticNodes::get);
    }

    public Map<String, Class<? extends TSyntheticNode>> getSyntheticNodeData() {
        return syntheticNodes;
    }

    public List<Class<? extends TSyntheticNode>> getSyntheticNodes() {
        return new ArrayList<>(syntheticNodes.values());
    }

    public void add(TSyntheticNodeTypeId id, Class<? extends TSyntheticNode> node) {
        Optional.ofNullable(id).map(StringSerializable::toString).ifPresent(nodeId ->
            syntheticNodes.put(nodeId, node)
        );
    }
}
