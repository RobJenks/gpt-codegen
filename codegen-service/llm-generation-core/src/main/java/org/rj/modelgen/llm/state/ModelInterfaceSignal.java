package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ModelInterfaceSignal {
    private final String id;
    private final String description;
    private Map<String, Object> payload = new HashMap<>();

    public ModelInterfaceSignal(String id) {
        this(id, null);
    }

    public <E extends Enum<E>> ModelInterfaceSignal(E id) {
        this(id.name(), null);
    }

    public <E extends Enum<E>> ModelInterfaceSignal(E id, String description) {
        this(id.name(), description);
    }

    public ModelInterfaceSignal(String id, String description) {
        this.id = Optional.ofNullable(id).orElseThrow(() -> new RuntimeException("No valid signal ID provided"));
        this.description = Optional.ofNullable(description).orElseGet(() -> defaultSignalDescription(id));
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    };

    @JsonIgnore
    public static String defaultSignalDescription(String id) {
        return String.format("Signal of type '%s'", id);
    }

    @JsonIgnore
    public boolean isSameSignalType(ModelInterfaceSignal otherSignal) {
        if (otherSignal == null) return false;
        return id.equals(otherSignal.id);
    }

    @JsonIgnore
    public <TSignal extends ModelInterfaceSignal> boolean isA(String id) {
        return this.id.equals(id);
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <TSignal extends ModelInterfaceSignal> Optional<TSignal> getAs(String id) {
        if (this.isA(id)) {
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
        if (key == null) return;
        this.payload.put(key, data);
    }

    public <E extends Enum<E>> void addPayloadData(E key, Object data) {
        if (key == null) return;
        this.payload.put(key.name(), data);
    }

    public void addPayloadDataIfAbsent(String key, Object data) {
        if (key == null) return;
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
