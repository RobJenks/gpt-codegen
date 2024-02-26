package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ModelInterfaceSignal {
    private final String id;
    private final String description;
    private ModelInterfacePayload payload = new ModelInterfacePayload();

    public ModelInterfaceSignal(String id) {
        this(id, null);
    }

    public <E extends Enum<E>> ModelInterfaceSignal(E id) {
        this(id, null);
    }

    public <E extends Enum<E>> ModelInterfaceSignal(E id, String description) {
        this(id.toString(), description);
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

    public ModelInterfacePayload getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        setPayload(new ModelInterfacePayload(payload));
    }

    public void setPayload(ModelInterfacePayload payload) {
        this.payload = Objects.requireNonNullElseGet(payload, ModelInterfacePayload::new);
    }

    public ModelInterfaceSignal withPayload(Map<String, Object> payload) {
        return withPayload(new ModelInterfacePayload(payload));
    }

    public ModelInterfaceSignal withPayload(ModelInterfacePayload payload) {
        setPayload(payload);
        return this;
    }

    public ModelInterfaceSignal withPayloadData(String key, Object data) {
        this.payload.put(key, data);
        return this;
    }

    public ModelInterfaceSignal withPayloadData(StandardModelData key, Object data) {
        this.payload.put(key, data);
        return this;
    }

    @JsonIgnore
    public Mono<ModelInterfaceSignal> mono() {
        return Mono.just(this);
    }

    @Override
    public String toString() {
        return id;
    }
}
