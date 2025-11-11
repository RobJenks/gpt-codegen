package org.rj.modelgen.llm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rj.modelgen.llm.intrep.graph.GraphConnection;
import org.rj.modelgen.llm.intrep.graph.GraphNode;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationUtils {

    private static final String VAR = "var";

    // BPMN internal names which are reserved and cannot be used as variable names inside scripts
    private static final Set<String> RESERVED_VARIABLE_NAMES = Set.of(
            "status"
    );

    public static <TConnection extends GraphConnection<String>, TNode extends GraphNode<String, String, TConnection>, TModel extends IntermediateGraphModel<String, String, TConnection, TNode>>
    List<String> identifyNumberOfRoots(TModel model) {
        final Map<String, Integer> referrers = model.getNodes().stream()
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

    public static Map<String, String> convertStringToMap(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }

    }
}
