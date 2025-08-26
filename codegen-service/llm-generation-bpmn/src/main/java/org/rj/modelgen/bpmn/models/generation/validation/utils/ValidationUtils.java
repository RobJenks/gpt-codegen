package org.rj.modelgen.bpmn.models.generation.validation.utils;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationUtils {

    private static final String VAR = "var";

    // HRS internal names which are reserved and cannot be used as variable names inside scripts
    private static final Set<String> RESERVED_VARIABLE_NAMES = Set.of(
            "status"
    );

    // Outputs which the model is allowed to generate even though they are listed as generated outputs in the
    // component library.  This is specifically to handle the "targetNode" case.  We should adjust the component library
    // definition to avoid adding it as a fixed generated output in future to avoid confusion
    private static final Set<String> ALLOWED_OUTPUT_NAME_CONFLICTS = Set.of(
            "targetNode"
    );

    public static List<String> identifyNumberOfRoots(BpmnIntermediateModel model) {
        final Map<String, Integer> referrers = model.getNodes().stream()
                .map(ElementNode::getName)
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
