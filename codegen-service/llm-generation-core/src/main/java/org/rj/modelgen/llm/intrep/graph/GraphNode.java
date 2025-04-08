package org.rj.modelgen.llm.intrep.graph;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Graph specialization for the model Intermediate Representation (IR)
 *
 * @param <TNodeId>         Type of the identifier used to uniquely-identify nodes in the graph
 * @param <TConnection>     Type of the connections made from this node to others (specific by their id : TNodeId)
 */
public interface GraphNode<TNodeId, TConnection extends GraphConnection<TNodeId>> {

    /**
     * All nodes require some identifier in order to maintain connections
     */
    TNodeId getId();
    void setId(TNodeId id);

    /**
     * Connection to other nodes in the graph
     */
    Collection<TConnection> getConnectedTo();
    void setConnectedTo(Collection<TConnection> connections);

    /**
     * Return the connection to the given node, IF a connection does exist
     */
    default Optional<TConnection> getConnectionTo(TNodeId targetNode) {
        if (targetNode == null || getConnectedTo() == null) return Optional.empty();
        return getConnectedTo().stream()
                .filter(Objects::nonNull)
                .filter(conn -> targetNode.equals(conn.getTargetNode()))
                .findFirst();
    }
}
