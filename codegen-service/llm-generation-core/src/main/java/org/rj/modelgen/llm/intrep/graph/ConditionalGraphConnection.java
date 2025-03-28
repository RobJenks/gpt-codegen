package org.rj.modelgen.llm.intrep.graph;

/**
 * Represents a connection within the generic graph specialization of the model Intermediate Representation (IR), where
 * the graph edge may have an associated condition
 *
 * @param <TNodeId>         Type of the identifier used to uniquely-identify nodes in the graph
 */
public interface ConditionalGraphConnection<TNodeId> extends GraphConnection<TNodeId> {

    /**
     * Return the condition associated with this graph connection, or null if none
     *
     * @return      Condition associated to this edge, or null if no condition exists
     */
    String getCondition();

    /**
     * Set the condition associated to this graph connection
     *
     * @param condition     Condition to be associated to this graph connection
     */
    void setCondition(String condition);
}
