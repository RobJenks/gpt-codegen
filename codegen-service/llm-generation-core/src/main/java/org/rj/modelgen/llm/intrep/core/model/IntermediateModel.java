package org.rj.modelgen.llm.intrep.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.util.Util;

public interface IntermediateModel {

    @JsonIgnore
    default String serialize() {
        return Util.serializeOrThrow(this);
    }
}
