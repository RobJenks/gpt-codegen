package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ModelInterfaceSignal {
    private final Class<? extends ModelInterfaceSignal> signalClass;
    private final String signalId;
    private Map<String, Object> payload = new HashMap<>();

    public ModelInterfaceSignal(Class<? extends ModelInterfaceSignal> cls) {
        this.signalClass = cls;
        this.signalId = defaultSignalId(cls);
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayloadDataAs(String key) {
        return (T)payload.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStandardPayloadData(ModelInterfaceStandardPayloadData item) {
        if (item == null) return null;
        return (T)getPayloadDataAs(item.getKey());
    }

    public void addPayloadData(String key, Object data) {
        this.payload.put(key, data);
    }

    public void addPayloadDataIfAbsent(String key, Object data) {
        this.payload.putIfAbsent(key, data);
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = Objects.requireNonNullElseGet(payload, HashMap::new);
    }

    // Standard payload data guaranteed by the ModelInterfaceStartSignal signature
    public String getSessionId() { return getStandardPayloadData(ModelInterfaceStandardPayloadData.SessionId); }
    public String getRequest() { return getStandardPayloadData(ModelInterfaceStandardPayloadData.Request); }
    public String getLlm() { return getStandardPayloadData(ModelInterfaceStandardPayloadData.LLM); }
    public float getTemperature() { return getStandardPayloadData(ModelInterfaceStandardPayloadData.Temperature); }
}
