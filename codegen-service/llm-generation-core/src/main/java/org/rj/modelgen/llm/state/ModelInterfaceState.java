package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.model.ModelInterface;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ModelInterfaceState<TInputSignal extends ModelInterfaceSignal> {
    private final Class<? extends ModelInterfaceState<? extends ModelInterfaceSignal>> stateClass;
    private final ModelInterfaceStateType type;
    private String id;
    private ModelInterfaceStateMachine model;
    private int invokeCount;
    private Integer invokeLimit;
    private Map<String, Object> inboundSignalMetadata;

    public ModelInterfaceState(Class<? extends ModelInterfaceState<? extends ModelInterfaceSignal>> cls) {
        this(cls, ModelInterfaceStateType.DEFAULT);
    }

    public ModelInterfaceState(Class<? extends ModelInterfaceState<? extends ModelInterfaceSignal>> cls, ModelInterfaceStateType type) {
        this.id = defaultStateId(cls);
        this.stateClass = cls;
        this.type = type;

        this.invokeCount = 0;
        this.invokeLimit = null;        // Limit is set if non-null
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public void overrideDefaultId(String newStateId) {
        this.id = newStateId;
    }

    public Class<? extends ModelInterfaceState<? extends ModelInterfaceSignal>> getStateClass() {
        return stateClass;
    }

    public ModelInterfaceStateType getType() {
        return type;
    }

    /**
     * Implemented by subclasses; return a text description of the state
     */
    @JsonIgnore
    public abstract String getDescription();

    @JsonIgnore
    public boolean isSameStateType(ModelInterfaceState<? extends ModelInterfaceSignal> otherState) {
        if (otherState == null) return false;
        return Objects.equals(id, otherState.id);
    }

    public void registerWithModel(ModelInterfaceStateMachine model) {
        this.model = model;
    }

    protected ModelInterface getModelInterface() {
        return Optional.ofNullable(model).map(ModelInterfaceStateMachine::getModelInterface).orElse(null);
    }

    public int getInvokeCount() {
        return invokeCount;
    }

    public Integer getInvokeLimit() {
        return invokeLimit;
    }

    @JsonIgnore
    public boolean hasInvokeLimit() {
        return invokeLimit != null;
    }

    public void setInvokeLimit(Integer invokeLimit) {
        this.invokeLimit = invokeLimit;
    }

    public boolean isTerminal() {
        return  type == ModelInterfaceStateType.TERMINAL_SUCCESS ||
                type == ModelInterfaceStateType.TERMINAL_FAILURE;
    }

    /**
     * Called by the model interface state machine when entering the new state.  Performs some basic operations
     * before delegating to subclasses for all action logic
     *
     * @param inputSignal       Signal received from the previous state
     * @return                  Output signal containing the result of this action
     */
    @JsonIgnore
    public Mono<ModelInterfaceSignal> invoke(ModelInterfaceSignal inputSignal) {
        this.invokeCount += 1;
        if (hasInvokeLimit() && invokeCount > invokeLimit) {
            return outboundSignal(new ModelInterfaceStandardSignals.FAIL_MAX_INVOCATIONS(id, invokeCount));
        }

        this.inboundSignalMetadata = inputSignal.getPayload();

        return invokeAction(inputSignal);
    }

    /**
     * Implemented by subclasses.  Perform all actions attached to this state, based on the input signal
     * received from the previous state, and output a new signal containing the results of this action
     *
     * @param inputSignal       Signal received from the previous state
     * @return                  Output signal containing the result of this action
     */
    @JsonIgnore
    protected abstract Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal);

    /**
     * Generates an outbound signal based on the provided data
     *
     * @param signal            Outbound signal data
     * @return                  Signal with all required data for the execution model
     */
    protected Mono<ModelInterfaceSignal> outboundSignal(ModelInterfaceSignal signal) {
        if (signal == null) throw new LlmGenerationModelException("Invalid null outbound signal at state: " + id);

        if (inboundSignalMetadata != null) {
            inboundSignalMetadata.forEach(signal::addPayloadDataIfAbsent);
        }

        return Mono.just(signal);
    }

    /**
     * Generates an outbound signal indicating model completion
     *
     * @return                  Terminal outbound signal
     */
    protected Mono<ModelInterfaceSignal> terminalSignal() {
        return Mono.empty();
    }

    /* Required unchecked cast due to Java type erasure.  But guaranteed by the type constraints on
       transition rules when the model is built */
    @SuppressWarnings("unchecked")
    protected TInputSignal asExpectedInputSignal(ModelInterfaceSignal signal) {
        return (TInputSignal)signal;
    }

    @JsonIgnore
    public static String defaultStateId(Class<? extends ModelInterfaceState> cls) {
        return cls.getSimpleName();
    }

    @JsonIgnore
    protected Mono<ModelInterfaceSignal> error(String message) {
        return outboundSignal(new ModelInterfaceStandardSignals.GENERAL_ERROR(id, message));
    }

    public <TState extends ModelInterfaceState<? extends ModelInterfaceSignal>>
    Optional<TState> getAs(Class<TState> cls) {
        if (stateClass == cls) {
            return Optional.of((TState)this);
        }

        return Optional.empty();
    }
}
