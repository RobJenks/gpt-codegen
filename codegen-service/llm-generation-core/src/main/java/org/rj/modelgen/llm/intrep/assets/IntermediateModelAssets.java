package org.rj.modelgen.llm.intrep.assets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.util.Util;

import java.util.List;

public class IntermediateModelAssets<TNodeUnresolvedInput extends NodeUnresolvedInput> {

    List<TNodeUnresolvedInput> unresolvedInputs;

    public IntermediateModelAssets() {
    }

    public List<TNodeUnresolvedInput> getUnresolvedInputs() {
        return unresolvedInputs;
    }

    public void setUnresolvedInputs(List<TNodeUnresolvedInput> unresolvedInputs) {
        this.unresolvedInputs = unresolvedInputs;
    }

    @JsonIgnore
    public String serialize() {
        return Util.serializeOrThrow(this);
    }

}
