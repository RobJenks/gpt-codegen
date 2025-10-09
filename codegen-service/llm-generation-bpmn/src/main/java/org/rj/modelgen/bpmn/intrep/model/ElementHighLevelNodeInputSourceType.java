package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ElementHighLevelNodeInputSourceType {
    @JsonProperty("NODE")
    NODE,
    @JsonProperty("CONSTANT")
    CONSTANT,
    @JsonProperty("GLOBAL")
    GLOBAL
}
