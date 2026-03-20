package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class SyntheticNodeConfig<TNodeId,
        TNodeName,
        TConnection extends GraphConnection<TNodeId>,
        TNode extends GraphNode<TNodeId, TNodeName, TConnection>,
        TModel extends IntermediateGraphModel<TNodeId, TNodeName, TConnection, TNode>,
        TSyntheticNodeTypeId extends StringSerializable,
        TSyntheticNode extends SyntheticNode<TNodeId, TNodeName, TConnection, TNode, TModel>,
        TSelf extends SyntheticNodeConfig<TNodeId, TNodeName, TConnection, TNode, TModel, TSyntheticNodeTypeId, TSyntheticNode, TSelf>> {

    private final Map<String, Class<? extends TSyntheticNode>> syntheticNodes;
    private final Map<String, Class<? extends TSyntheticNode>> resolvedToSyntheticTypeMapping;

    public SyntheticNodeConfig() {
        this(new HashMap<>());
    }

    public SyntheticNodeConfig(Map<TSyntheticNodeTypeId, Class<? extends TSyntheticNode>> syntheticNodes) {
        this.syntheticNodes = Optional.ofNullable(syntheticNodes).orElseGet(Map::of)
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue,
                        (a, b) -> b, HashMap::new));
        this.resolvedToSyntheticTypeMapping = new HashMap<>();
    }

    public Optional<Class<? extends TSyntheticNode>> getNode(TSyntheticNodeTypeId id) {
        return Optional.ofNullable(id)
                .map(StringSerializable::toString)
                .flatMap(this::getNode);
    }

    public Optional<Class<? extends TSyntheticNode>> getNode(String id) {
        return Optional.ofNullable(id).map(syntheticNodes::get);
    }

    public Map<String, Class<? extends TSyntheticNode>> getSyntheticNodeData() {
        return syntheticNodes;
    }

    public List<String> getTypeNames() {
        return new ArrayList<>(syntheticNodes.keySet());
    }

    public List<Class<? extends TSyntheticNode>> getSyntheticNodes() {
        return new ArrayList<>(syntheticNodes.values());
    }

    public void add(TSyntheticNodeTypeId id, Class<? extends TSyntheticNode> node) {
        Optional.ofNullable(id).map(StringSerializable::toString).ifPresent(nodeId ->
                syntheticNodes.put(nodeId, node)
        );
    }

    @SuppressWarnings("unchecked")
    public TSelf with(TSyntheticNodeTypeId id, Class<? extends TSyntheticNode> node) {
        add(id, node);
        return (TSelf) this;
    }

    /**
     * Register a mapping from a resolved node type to its synthetic node type.
     * This allows the unresolve operation to find nodes by their resolved type.
     *
     * @param resolvedType   The type that nodes become after resolution
     * @param syntheticType  The original synthetic type
     */
    public TSelf from(String resolvedType, Class<? extends TSyntheticNode> syntheticType) {
        resolvedToSyntheticTypeMapping.put(resolvedType, syntheticType);
        return (TSelf) this;
    }

    /**
     * Get all resolved types that map to the given synthetic type.
     *
     * @param syntheticType  The synthetic type
     * @return               List of resolved types
     */
    public List<String> getResolvedTypesForSyntheticType(Class<? extends TSyntheticNode> syntheticType) {
        return resolvedToSyntheticTypeMapping.entrySet().stream()
                .filter(e -> syntheticType.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
