package org.rj.modelgen.llm.synthetic.nodes;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.synthetic.SyntheticNode;
import org.rj.modelgen.llm.util.StringSerializable;

public abstract class TypeChangeSyntheticNode<
            TNodeId,
            TConnection extends GraphConnection<TNodeId>,
            TNode extends GraphNode<TNodeId, TConnection>,
            TModel extends IntermediateGraphModel<TNodeId, TConnection, TNode>,
            TTypeId extends StringSerializable
        >
        implements SyntheticNode<TNodeId, TConnection, TNode, TModel> {

    private final TTypeId newType;

    protected TypeChangeSyntheticNode(TTypeId newType) {
        this.newType = newType;
    }

    @Override
    public void resolve(TModel model, TNode node) {
        if (node == null) return;
        setNodeType(node, newType);
    }

    /**
     * Provided by implementing model type; set underlying type of this node
     *
     * @param node      Node to be updated
     * @param type      New type of the node
     */
    protected abstract void setNodeType(TNode node, TTypeId type);
}
