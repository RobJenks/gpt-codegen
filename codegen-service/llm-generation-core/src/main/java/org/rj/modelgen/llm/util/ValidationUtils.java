package org.rj.modelgen.llm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ValidationUtils {

    public static <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    List<String> identifyNumberOfRoots(TModel model) {
        return identifyNumberOfRoots(model, node -> true);
    }

    public static <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    List<String> identifyNumberOfRoots(TModel model, Predicate<TNode> nodeFilter) {
        final Map<String, Integer> referrers = model.getNodes().stream()
                .filter(nodeFilter)
                .map(GraphNode::getId)
                .collect(Collectors.toMap(Function.identity(), __ -> 0));

        // Count references to each node
        for (final var node : model.getNodes()) {
            node.getConnectedTo().forEach(conn -> {
                referrers.putIfAbsent(conn.getTargetNode(), 0);

                referrers.computeIfPresent(conn.getTargetNode(), (k, v) -> v + 1);
            });
        }

        return referrers.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList();
    }
}
