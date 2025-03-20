package org.rj.modelgen.llm.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.util.StringSerializable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Transition rule { (CurrentState, OutputSignal) -> NextState }
 */

public class ModelInterfaceTransitionRule {
    private final ModelInterfaceState currentState;
    private final String outputSignalId;
    private final ModelInterfaceState nextState;

    public <E extends Enum<E>> ModelInterfaceTransitionRule(ModelInterfaceState currentState, E outputSignalId, ModelInterfaceState nextState) {
        this(currentState, outputSignalId.toString(), nextState);
    }

    public ModelInterfaceTransitionRule(ModelInterfaceState currentState, String outputSignalId, ModelInterfaceState nextState) {
        this.currentState = currentState;
        this.outputSignalId = outputSignalId;
        this.nextState = nextState;
    }

    public ModelInterfaceState getCurrentState() {
        return currentState;
    }

    public String getOutputSignalId() {
        return outputSignalId;
    }

    public ModelInterfaceState getNextState() {
        return nextState;
    }

    @JsonIgnore
    public Reference getReference() {
        return new Reference(
                currentState != null ? currentState.getId() : null,
                outputSignalId,
                nextState != null ? nextState.getId() : null
        );
    }

    @Override
    public String toString() {
        return String.format("TransitionRule ((%s, %s) -> %s)",
                currentState != null ? currentState.getId() : "<null>",
                outputSignalId != null ? outputSignalId : "<null>",
                nextState != null ? nextState.getId() : "<null>");
    }

    @JsonIgnore
    public boolean matches(ModelInterfaceState currentState, String outputSignalId) {
        return  this.currentState.isSameStateType(currentState) &&
                this.outputSignalId.equals(outputSignalId);
    }

    @JsonIgnore
    public boolean isValid() {
        return  currentState != null &&
                outputSignalId != null &&
                nextState != null;
    }

    @JsonIgnore
    public boolean referencesState(String stateId) {
        if (stateId == null) return false;

        return (currentState != null && stateId.equals(currentState.getId())) ||
               (nextState != null && stateId.equals(nextState.getId()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelInterfaceTransitionRule that = (ModelInterfaceTransitionRule) o;
        return Objects.equals(currentState, that.currentState) && Objects.equals(outputSignalId, that.outputSignalId) && Objects.equals(nextState, that.nextState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentState, outputSignalId, nextState);
    }

    @JsonIgnore
    public boolean equalsReference(Reference reference) {
        if (reference == null) return false;
        return
                Objects.equals(reference.currentStateId, Optional.ofNullable(currentState).map(ModelInterfaceState::getId).orElse(null)) &&
                Objects.equals(reference.outputSignalId, outputSignalId) &&
                Objects.equals(reference.nextStateId, Optional.ofNullable(nextState).map(ModelInterfaceState::getId).orElse(null));
    }

    /**
     * ID-specified reference to an existing rule
     */
    public static class Reference {
        private String currentStateId;
        private String outputSignalId;
        private String nextStateId;

        public Reference() { }
        public Reference(String currentStateId, String outputSignalId, String nextStateId) {
            this.currentStateId = currentStateId;
            this.outputSignalId = outputSignalId;
            this.nextStateId = nextStateId;
        }

        public Reference(StringSerializable currentStateId, String outputSignalId, StringSerializable nextStateId) {
            this(Optional.ofNullable(currentStateId).map(StringSerializable::toString).orElse(null),
                 outputSignalId,
                 Optional.ofNullable(nextStateId).map(StringSerializable::toString).orElse(null));
        }

        public Reference(StringSerializable currentStateId, StringSerializable outputSignalId, StringSerializable nextStateId) {
            this(currentStateId,
                 Optional.ofNullable(outputSignalId).map(StringSerializable::toString).orElse(null),
                 nextStateId);
        }

        public String getCurrentStateId() {
            return currentStateId;
        }

        public void setCurrentStateId(String currentStateId) {
            this.currentStateId = currentStateId;
        }

        public String getOutputSignalId() {
            return outputSignalId;
        }

        public void setOutputSignalId(String outputSignalId) {
            this.outputSignalId = outputSignalId;
        }

        public String getNextStateId() {
            return nextStateId;
        }

        public void setNextStateId(String nextStateId) {
            this.nextStateId = nextStateId;
        }

        @JsonIgnore
        public boolean equalsRule(ModelInterfaceTransitionRule rule) {
            if (rule == null) return false;
            return rule.equalsReference(this);
        }

        @JsonIgnore
        public Optional<ModelInterfaceTransitionRule> resolveToRule(Collection<ModelInterfaceState> availableStates) {
            if (availableStates == null) return Optional.empty();
            if (currentStateId == null || nextStateId == null) return Optional.empty();

            final BiFunction<String, Collection<ModelInterfaceState>, Optional<ModelInterfaceState>> resolve = (id, states) -> {
                if (id == null) return Optional.empty();
                return states.stream().filter(state -> id.equals(state.getId())).findFirst();
            };

            final var current = resolve.apply(currentStateId, availableStates);
            final var next = resolve.apply(nextStateId, availableStates);
            if (current.isEmpty() || next.isEmpty()) return Optional.empty();

            return Optional.of(new ModelInterfaceTransitionRule(current.get(), outputSignalId, next.get()));
        }
    }
}
