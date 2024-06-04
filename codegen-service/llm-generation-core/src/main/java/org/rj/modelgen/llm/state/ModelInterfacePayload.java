package org.rj.modelgen.llm.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ModelInterfacePayload {

    private Map<String, Object> data = new HashMap<>();


    public ModelInterfacePayload() { }

    public ModelInterfacePayload(Map<String, Object> data) {
        this.data = Optional.ofNullable(data).orElseGet(HashMap::new);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void put(String key, Object data) {
        if (key == null) return;
        this.data.put(key, data);
    }

    public <E extends Enum<E>> void put(E key, Object data) {
        if (key == null) return;
        this.data.put(key.toString(), data);
    }

    public void putIfAbsent(String key, Object data) {
        if (key == null) return;
        this.data.putIfAbsent(key, data);
    }

    public void putAllIfAbsent(ModelInterfacePayload payload) {
        if (payload == null) return;
        putAllIfAbsent(payload.getData());
    }

    public void putAllIfAbsent(Map<String, Object> payloadData) {
        if (payloadData == null) return;
        payloadData.forEach(data::putIfAbsent);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T)data.get(key);
    }

    public <T, E extends Enum<E>> T get(E key) {
        if (key == null) return null;
        return get(key.toString());
    }

    public <T> T getOrElse(String key, T defaultValue) {
        return getOrElse(key, () -> defaultValue);
    }

    public <T, E extends Enum<E>> T getOrElse(E key, T defaultValue) {
        return getOrElse(key.toString(), defaultValue);
    }

    public <T, E extends Enum<E>> T getOrElse(E key, Supplier<T> defaultValue) {
        if (key == null) return defaultValue.get();
        return getOrElse(key.toString(), defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrElse(String key, Supplier<T> defaultValue) {
        return Optional.ofNullable((T)data.get(key)).orElseGet(defaultValue);
    }

    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    public <E extends Enum<E>> boolean hasData(E key) {
        if (key == null) return false;
        return data.containsKey(key.toString());
    }

    public <T, E extends Enum<E>> T getOrThrow(E key, Supplier<RuntimeException> onMissing) {
        if (key == null) throw new IllegalArgumentException("Null key provided");
        return getOrThrow(key.toString(), onMissing);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrThrow(String key, Supplier<RuntimeException> onMissing) {
        return Optional.ofNullable((T)data.get(key)).orElseThrow(onMissing);
    }

    public void setData(Map<String, Object> data) {
        this.data = Objects.requireNonNullElseGet(data, HashMap::new);
    }

    public ModelInterfacePayload withData(String key, Object data) {
        put(key, data);
        return this;
    }

    public <E extends Enum<E>> ModelInterfacePayload withData(E key, Object data) {
        put(key, data);
        return this;
    }

}
