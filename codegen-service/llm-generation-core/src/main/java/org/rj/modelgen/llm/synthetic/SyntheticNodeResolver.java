package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.List;
import java.util.Optional;

public class SyntheticNodeResolver<TNodeId,
        TNodeName,
        TConnection extends GraphConnection<TNodeId>,
        TNode extends GraphNode<TNodeId, TNodeName, TConnection>,
        TModel extends IntermediateGraphModel<TNodeId, TNodeName, TConnection, TNode>,
        TSyntheticNodeTypeId extends StringSerializable,
        TSyntheticNode extends SyntheticNode<TNodeId, TNodeName, TConnection, TNode, TModel>,
        TConfig extends SyntheticNodeConfig<TNodeId, TNodeName, TConnection, TNode, TModel, TSyntheticNodeTypeId, TSyntheticNode, ?>> {

    private final TConfig config;

    public SyntheticNodeResolver(TConfig config) {
        this.config = config;
    }

    public void resolve(TSyntheticNodeTypeId type, TNode node, TModel model) {
        resolve(Optional.ofNullable(type).map(StringSerializable::toString).orElse(null), node, model);
    }

    public void resolve(String type, TNode node, TModel model) {
        if (config == null) return;

        final var syntheticNodeClass = config.getNode(type);
        if (syntheticNodeClass.isEmpty()) return;

        try {
            final var syntheticNode = syntheticNodeClass.get().getDeclaredConstructor().newInstance();
            syntheticNode.resolve(model, node);
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to resolve synthetic node type '%s'".formatted(type), ex);
        }
    }

    public List<String> getResolvableTypes() {
        return Optional.ofNullable(config)
                .map(SyntheticNodeConfig::getTypeNames)
                .orElseGet(List::of);
    }

}
