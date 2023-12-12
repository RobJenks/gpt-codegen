package org.rj.modelgen.llm.schema.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.util.Util;

import java.util.ArrayList;
import java.util.List;

public class IntermediateModel {
    private List<ElementNode> nodes = new ArrayList<>();

    public IntermediateModel() {
    }

    public List<ElementNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ElementNode> nodes) {
        this.nodes = nodes;
    }

    @JsonIgnore
    public String serialize() {
        return Util.serializeOrThrow(this);
    }
}
