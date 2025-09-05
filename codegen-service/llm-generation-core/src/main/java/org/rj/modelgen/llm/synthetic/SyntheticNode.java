package org.rj.modelgen.llm.synthetic;

import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

public interface SyntheticNode<TNodeId,
                               TNodeName,
                               TConnection extends GraphConnection<TNodeId>,
                               TNode extends GraphNode<TNodeId, TNodeName, TConnection>,
                               TModel extends IntermediateGraphModel<TNodeId, TNodeName, TConnection, TNode>> {

    /**
     * Resolve the given synthetic node into the model.  May mutate the input model and node
     *
     * @param model            The current model state
     * @param syntheticNode    The node to be resolved
     */
    void resolve(TModel model, TNode syntheticNode);

}
