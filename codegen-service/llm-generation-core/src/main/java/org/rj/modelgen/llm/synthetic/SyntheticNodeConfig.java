package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.*;
import java.util.stream.Collectors;

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

    public SyntheticNodeConfig(Map<TSyntheticNodeTypeId, Class<? extends TSyntheticNode>> syntheticNodes) {
        this.syntheticNodes = Optional.ofNullable(syntheticNodes).orElseGet(Map::of)
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue,
                        (a, b) -> b, HashMap::new));
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
