package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ModelInterfaceSignal {
    private final Class<? extends ModelInterfaceSignal> signalClass;
    private final String signalId;
    private Map<String, Object> metadata;

    public ModelInterfaceSignal(Class<? extends ModelInterfaceSignal> cls) {
        this(cls, new HashMap<>());
    }

    public ModelInterfaceSignal(Class<? extends ModelInterfaceSignal> cls, Map<String, Object> metadata) {
        this.signalClass = cls;
        this.signalId = defaultSignalId(cls);
        this.metadata = metadata;
    }

    public String getSignalId() {
        return signalId;
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
        return signalClass == otherSignal.signalClass;
    }

    @JsonIgnore
    public <TSignal extends ModelInterfaceSignal> boolean isA(Class<TSignal> signalClass) {
        return this.signalClass == signalClass;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <TSignal extends ModelInterfaceSignal> Optional<TSignal> getAs(Class<TSignal> signalClass) {
        if (isA(signalClass)) {
            return Optional.of((TSignal)this);
        }

        return Optional.empty();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object data) {
        this.metadata.put(key, data);
    }

    public void addMetadataIfAbsent(String key, Object data) {
        this.metadata.putIfAbsent(key, data);
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNullElseGet(metadata, HashMap::new);
    }
}
