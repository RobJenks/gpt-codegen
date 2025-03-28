package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.util.StringSerializable;

public class SyntheticNodeResolver<TNodeId,
        TConnection extends GraphConnection<TNodeId>,
        TNode extends GraphNode<TNodeId, TConnection>,
        TModel extends IntermediateGraphModel<TNodeId, TConnection, TNode>,
        TSyntheticNodeTypeId extends StringSerializable,
        TSyntheticNode extends SyntheticNode<TNodeId, TConnection, TNode, TModel>,
        TConfig extends SyntheticNodeConfig<TNodeId, TConnection, TNode, TModel, TSyntheticNodeTypeId, TSyntheticNode, ?>> {

    private TConfig config;

    public SyntheticNodeResolver(TConfig config) { }

    public void resolve(TSyntheticNodeTypeId type, TNode node, TModel model) {
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
}
