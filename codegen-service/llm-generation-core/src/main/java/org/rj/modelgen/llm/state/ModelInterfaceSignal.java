package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ModelInterfaceSignal {
    private final String id;
    private Map<String, Object> metadata;

    public ModelInterfaceSignal(Class<? extends ModelInterfaceSignal> cls) {
        this(cls, new HashMap<>());
    }

    public ModelInterfaceSignal(Class<? extends ModelInterfaceSignal> cls, Map<String, Object> metadata) {
        this.id = defaultSignalId(cls);
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
    public static String defaultSignalId(Class<? extends ModelInterfaceSignal> cls) {
        return cls.getSimpleName();
    }

    @JsonIgnore
    public boolean isSameSignalType(ModelInterfaceSignal otherSignal) {
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
