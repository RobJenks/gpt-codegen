package org.rj.modelgen.llm.state;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.audit.ModelInterfaceStateMachineAuditLog;
import org.rj.modelgen.llm.beans.AuditEntry;
import org.rj.modelgen.llm.exception.LlmGenerationConfigException;
import org.rj.modelgen.llm.model.ModelInterface;
import org.rj.modelgen.llm.statemodel.signals.common.StandardErrorSignals;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelInterfaceStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(ModelInterfaceStateMachine.class);

    private final Class<? extends ModelInterfaceStateMachine> modelClass;
    private final ModelInterface modelInterface;
    protected final Map<String, ModelInterfaceState> states;
    private final ModelInterfaceTransitionRules rules;
    private final List<Consumer<ModelInterfaceStateEmittedSignal>> stateListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<AuditEntry>> auditListeners = new CopyOnWriteArrayList<>();

    private final ModelInterfaceStateMachineAuditLog auditLog;

    // Default states built in to all models
    private final ModelInterfaceState defaultStateError = new ModelInterfaceStandardStates.FAILED_WITH_ERROR();
    private final ModelInterfaceState defaultStateNoRule = new ModelInterfaceStandardStates.NO_TRANSITION_RULE();
    private final ModelInterfaceState defaultStateMaxInvocations = new ModelInterfaceStandardStates.EXCEEDED_MAX_INVOCATIONS();

    public ModelInterfaceStateMachine(Class<? extends ModelInterfaceStateMachine> modelClass,
                                      ModelInterface modelInterface, List<ModelInterfaceState> states,
                                      ModelInterfaceTransitionRules rules) {
        this.modelClass = modelClass;
        this.modelInterface = modelInterface;
        this.states = Optional.ofNullable(states).orElseGet(List::of).stream()
                .collect(Collectors.toMap(ModelInterfaceState::getId, Function.identity(),
                        (a, b) -> { throw new IllegalArgumentException("Cannot build model; invalid duplicate state ID: " + a.getId()); }));
        this.rules = Optional.ofNullable(rules).orElseGet(() -> new ModelInterfaceTransitionRules(List.of()));

        this.states.values().forEach(state -> state.registerWithModel(this));

        this.auditLog = new ModelInterfaceStateMachineAuditLog();
    }

    public <TPayload extends ModelInterfaceInputPayload, E extends Enum<E>>
    Mono<ModelInterfaceExecutionResult> execute(String initialState, E inputSignal, TPayload payload) {
        if (inputSignal == null) throw new LlmGenerationConfigException("Cannot start execution; no valid input signal");
        return execute(initialState, inputSignal.toString(), payload);
    }

    public <TPayload extends ModelInterfaceInputPayload>
    Mono<ModelInterfaceExecutionResult> execute(String initialState, String inputSignal, TPayload payload) {
        LOG.info("Executing state model interface from initial state '{}'", initialState);
        final var init = Optional.ofNullable(states).map(x -> x.get(initialState))
                .orElseThrow(() -> new LlmGenerationConfigException(String.format("Cannot start execution; initial state '%s' not found", initialState)));

        final var startSignal = new ModelInterfaceStartSignal<>(inputSignal, payload);

        final Mono<List<ModelInterfaceStateWithInputSignal>> execution = Mono.just(new ModelInterfaceStateWithInputSignal(init, startSignal))
                .expand(this::executeStep)
                .collectList();

        return execution.map(this::buildResult);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Mono<ModelInterfaceStateWithInputSignal>
    executeStep(ModelInterfaceStateWithInputSignal input) {
        LOG.info("Model interface executing state '{}' with input signal '{}'",
                input.getState().getId(), input.getInputSignal());

        // If this is a terminal state then invoke it and end the execution immediately
        if (input.getState().isTerminal()) {
            return input.getState().invoke(input.getInputSignal())
                    .flatMap(__ -> Mono.empty());   // No output signal from a terminal state
        }

        // Execute the action associated with this state
        return input.getState().invoke(input.getInputSignal())
                .map(outputSignal -> {
                    // Terminate execution if we exceeded the maximum allowed invocations of a state, AND we don't
                    // have an explicit rule to handle that error signal type
                    if (outputSignal.isA(StandardErrorSignals.FAILED_MAX_INVOCATIONS)) {
                        if (!rules.hasRule(input.getState(), StandardErrorSignals.FAILED_MAX_INVOCATIONS)) {
                            return new ModelInterfaceStateWithInputSignal(defaultStateMaxInvocations, outputSignal);
                        }
                    }

                    // Attempt to find a matching rule based on this state and the action output
                    return rules.find(input.getState(), outputSignal)

                            // Transition rule exists; move to the next step
                            .map(rule -> new ModelInterfaceStateWithInputSignal(rule.getNextState(), outputSignal))

                            // No matching transition rule
                            .orElseGet(() ->

                                    // Special-case: route any unhandled error signals to the global error handler state
                                    outputSignal.getAs(StandardErrorSignals.GENERAL_ERROR)
                                            .map(error -> {
                                                LOG.error("Unhandled Error Signal during state machine transition: [{}]", error);
                                                return new ModelInterfaceStateWithInputSignal(defaultStateError, outputSignal);
                                            })

                                    // Not an error, so route to the 'no matching rule' end state
                                    .orElseGet(() -> new ModelInterfaceStateWithInputSignal(defaultStateNoRule,
                                            new ModelInterfaceStandardSignals.FAIL_NO_MATCHING_TRANSITION_RULE(input.getState().getId(), outputSignal.getId())))
                            );
                    });
    }

    public ModelInterfaceStateMachine withModelCustomization(Function<ModelCustomizationData, ModelInterfaceStateMachineCustomization> modification) {
        final var currentState = new ModelCustomizationData(this, new ModelData(this.states.values().stream().toList(), this.rules));
        final var changes = modification.apply(currentState);

        // Insert states between existing states first, since subsequent changes may change existing states
        if (changes.getInsertStateAfter() != null) {
            changes.getInsertStateAfter().forEach(change -> insertStateAfter(change.getLeft(), change.getRight()));
        }

        // Remove states before adding, in case the caller is trying to replace an existing states.  Also remove rules which reference them
        if (changes.getRemovedStates() != null) {
            rules.getRules().removeIf(rule ->
                    changes.getRemovedStates().contains(rule.getCurrentState().getId()) ||
                    changes.getRemovedStates().contains(rule.getNextState().getId()));
            states.entrySet().removeIf(entry -> changes.getRemovedStates().contains(entry.getValue().getId()));
        }

        // Add states
        for (ModelInterfaceState state : Optional.ofNullable(changes.getNewStates()).orElseGet(List::of)) {
            addState(state);
        }

        // Remove rules
        for (final var remove : Optional.ofNullable(changes.getRemovedRules()).orElseGet(List::of)) {
            rules.getRules().removeIf(rule -> rule.equalsReference(remove));
        }

        // Add rules
        for (final var newRule : Optional.ofNullable(changes.getNewRules()).orElseGet(List::of)) {
            newRule.resolveToRule(states.values())
                    .ifPresent(rule -> rules.getRules().add(rule));
        }

        return this;
    }

    private void addState(ModelInterfaceState state) {
        if (state == null) return;

        states.put(state.getId(), state);
        state.registerWithModel(this);
    }

    private void addRule(ModelInterfaceTransitionRule rule) {
        rules.addRule(rule);
    }

    private void insertStateAfter(ModelInterfaceState state, String insertAfter) {
        if (state == null || StringUtils.isEmpty(insertAfter)) return;

        final var preNode = states.getOrDefault(insertAfter, null);
        if (preNode == null) return;

        // Add the new state
        addState(state);

        // If the pre-node has success connection to another (A->B), reroute through the new node (A->X->B)
        // This shortcut method is only suitable where we want to insert in the normal success path, since we
        // can't otherwise know how to make custom connections
        rules.find(preNode, preNode.getSuccessSignalId()).ifPresent(preConnection -> {
           final var postNode = preConnection.getNextState();
           preConnection.setNextState(state);                                                        // Connect A->X
           addRule(new ModelInterfaceTransitionRule(state, state.getSuccessSignalId(), postNode));   // Connect X->B
        });
    }

    protected ModelInterfaceExecutionResult buildResult(List<ModelInterfaceStateWithInputSignal> steps) {
        return new ModelInterfaceExecutionResult(
                steps.get(steps.size() - 1).getState(),
                steps
        );
    }

    public ModelInterface getModelInterface() {
        return modelInterface;
    }

    public void recordAudit(ModelInterfaceState state, String sessionId, String identifier, String content) {
        auditLog.recordAudit(this, state, sessionId, identifier, content);
    }

    /**
     * Return the model as the given subclass.  Obviously only safe where the type is statically-known
     * Will throw if an invalid class is provided.  Implemented this way to avoid CRTP on the base
     * model which would make all model components very unwieldy to use
     *
     * @param cls       Subclass type
     * @return          Model as the implementation subtype
     */
    public<TSelf extends ModelInterfaceStateMachine> TSelf getAs(Class<TSelf> cls) {
        if (cls != modelClass) {
            throw new IllegalArgumentException(String.format("Cannot return model as a '%s'; this is a '%s'",
                    (cls != null ? cls.getSimpleName() : "<null>"),
                    (modelClass != null ? modelClass.getSimpleName() : "<null>")));
        }

        return cls.cast(this);
    }

    public Set<ModelInterfaceTransitionRule.Reference> getLayoutInfo() {
        return rules.getReferences();
    }

    public String getDebugLayoutInfo() {
        return Util.serializeOrThrow(getLayoutInfo());
    }

    public static class ModelData {
        private final List<ModelInterfaceState> states;
        private final ModelInterfaceTransitionRules rules;

        public ModelData(List<ModelInterfaceState> states, ModelInterfaceTransitionRules rules) {
            this.states = states;
            this.rules = rules;
        }

        public List<ModelInterfaceState> getStates() {
            return states;
        }

        public ModelInterfaceTransitionRules getRules() {
            return rules;
        }
    }

    public static class ModelCustomizationData {
        private final ModelInterfaceStateMachine model;
        private final ModelData data;

        public ModelCustomizationData(ModelInterfaceStateMachine model, ModelData data) {
            this.model = model;
            this.data = data;
        }

        public ModelInterfaceStateMachine getModel() {
            return model;
        }

        public ModelData getData() {
            return data;
        }
    }

    public void addStateListener(Consumer<ModelInterfaceStateEmittedSignal> listener) {
        stateListeners.add(listener);
    }

    public void publishStateListener(ModelInterfaceStateEmittedSignal event) {
        stateListeners.forEach(listener -> listener.accept(event));
    }

    public void addAuditListener(Consumer<AuditEntry> listener) {
        auditListeners.add(listener);
    }

    public void publishAuditListener(AuditEntry event) {
        auditListeners.forEach(listener -> listener.accept(event));
    }

}
