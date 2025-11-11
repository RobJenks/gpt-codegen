package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BpmnHighLevelIntermediateModel extends IntermediateGraphModel<String, String, ElementConnection, ElementHighLevelNode> {

    public BpmnHighLevelIntermediateModel() {
        super();
    }

    public JSONObject toJson() {
        return new JSONObject(serialize());
    }

    public String generateSummary() {
        return getNodes().stream()
                .map(ElementHighLevelNode::generateSummary)
                .collect(Collectors.joining("\n"));
    }
}
