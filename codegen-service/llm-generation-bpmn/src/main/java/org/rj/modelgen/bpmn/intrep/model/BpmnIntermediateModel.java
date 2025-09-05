package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

import static org.rj.modelgen.llm.util.Util.deserializeOrThrow;

/**
 * Implementation of graph-specialized IR for BPMN models
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BpmnIntermediateModel extends IntermediateGraphModel<String, String, ElementConnection, ElementNode> {
    public BpmnIntermediateModel() {
        super();
    }

    public static BpmnIntermediateModel fromJson(JSONObject json) {
        return deserializeOrThrow(json.toString(), BpmnIntermediateModel.class);
    }

    public JSONObject toJson() {
        return new JSONObject(serialize());
    }
}
