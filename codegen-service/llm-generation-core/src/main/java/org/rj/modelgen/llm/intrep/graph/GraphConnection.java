package org.rj.modelgen.llm.intrep.graph;

/**
 * Represents a connection within the generic graph specialization of the model Intermediate Representation (IR)
 *
 * @param <TNodeId>         Type of the identifier used to uniquely-identify nodes in the graph
 */
public interface GraphConnection<TNodeId> {

    /**
     * Return the target of this connection.  Each connection has exactly one target node
     */
    TNodeId getTargetNode();

    /**
     * Set the target of this connection.  Each connection has exactly one target node
     */
    void setTargetNode(TNodeId targetNode);

}
