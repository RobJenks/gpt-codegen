package org.rj.modelgen.llm.intrep.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IntermediateGraphModel<TNodeId,
                                    TNodeName,
                                    TConnection extends GraphConnection<TNodeId>,
                                    TNode extends GraphNode<TNodeId, TNodeName, TConnection>> implements IntermediateModel {

    private List<TNode> nodes = new ArrayList<>();

    public IntermediateGraphModel() { }

    public List<TNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TNode> nodes) {
        this.nodes = nodes;
    }

    // Returns a stream of all nodes in this graph AND any subgraphs, for implementations where graphs can
    // contain other graphs.  Will be identical to `getNodes()` for implementations which do not support
    // this.  Stream makes no guarantee about ordering.  Individual nodes can be modified but obviously not
    // added or removed since this is a stream API
    @JsonIgnore
    public Stream<TNode> getAllNodesRecursive() {
        return getNodes().stream();
    }

}
