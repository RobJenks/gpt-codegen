package org.rj.modelgen.llm.intrep.graph;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;

import java.util.ArrayList;
import java.util.List;

public class IntermediateGraphModel<TNodeId,
                                    TConnection extends GraphConnection<TNodeId>,
                                    TNode extends GraphNode<TNodeId, TConnection>> implements IntermediateModel {

    private List<TNode> nodes = new ArrayList<>();

    public IntermediateGraphModel() { }

    public List<TNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TNode> nodes) {
        this.nodes = nodes;
    }

}
