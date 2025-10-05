package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ElementHighLevelNodeInputSourceType {
    @JsonProperty("node")
    Node,
    @JsonProperty("constant")
    Constant,
    @JsonProperty("global")
    Global
}
