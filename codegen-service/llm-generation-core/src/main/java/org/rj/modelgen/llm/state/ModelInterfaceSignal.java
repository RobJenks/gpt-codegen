package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ModelInterfaceSignal<TTargetState> {
    private final String id;
    private Map<String, Object> metadata;

    public ModelInterfaceSignal(String id) {
        this(id, new HashMap<>());
    }

    public ModelInterfaceSignal(String id, Map<String, Object> metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    /**
     * Implemented by subclasses; return a text description of the signal
     */
    @JsonIgnore
    public abstract String getDescription();

    @JsonIgnore
    public boolean isSameSignalType(ModelInterfaceSignal<? extends ModelInterfaceState> otherSignal) {
        if (otherSignal == null) return false;
        return Objects.equals(id, otherSignal.id);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNullElseGet(metadata, HashMap::new);
    }


}
