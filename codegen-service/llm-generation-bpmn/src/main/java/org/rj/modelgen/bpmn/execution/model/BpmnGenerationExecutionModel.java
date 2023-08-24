package org.rj.modelgen.bpmn.execution.model;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.bpmn.execution.signal.RequestSubmissionToLlmSignal;
import org.rj.modelgen.bpmn.execution.signal.StartBpmnGenerationSignal;
import org.rj.modelgen.bpmn.execution.state.PrepareBpmnModelGenerationRequest;
import org.rj.modelgen.bpmn.execution.state.StartBpmnGeneration;
import org.rj.modelgen.bpmn.execution.state.SubmitBpmnGenerationRequestToLlm;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class BpmnGenerationExecutionModel {
    private static final Logger LOG = LoggerFactory.getLogger(BpmnGenerationExecutionModel.class);

    public static ModelInterfaceStateMachine create(String currentIL, String request) {
        final var stateInit = new StartBpmnGeneration();
        final var statePrepareRequest = new PrepareBpmnModelGenerationRequest();
        final var stateSubmitToLlm = new SubmitBpmnGenerationRequestToLlm();

        final var states = List.of(stateInit, statePrepareRequest, stateSubmitToLlm);

        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule<>(stateInit, PrepareLlmRequestSignal.class, statePrepareRequest),
                new ModelInterfaceTransitionRule<>(statePrepareRequest, RequestSubmissionToLlmSignal.class, stateSubmitToLlm)
        ));

        final var model = new ModelInterfaceStateMachine(states, rules);

        final var result = model.execute("StartBpmnGeneration", new StartBpmnGenerationSignal("START")).block(Duration.ofSeconds(20L));
        if (result != null) {
            System.out.printf("Result: %s (%s)\n", result.getResult().getId(), result.getResult().getDescription());
            System.out.printf("Execution path:\nSTART\n%s\n", result.getExecutionPath().stream()
                    .map(x -> String.format("Signal [%s (%s)]  -->  %s (%s)", x.getInputSignal().getId(), x.getInputSignal().getDescription(),
                            x.getState().getId(), x.getState().getDescription()))
                    .collect(Collectors.joining("\n")));
        }
        else {
            System.out.println("Execution failed - no result");
        }

        return null;
    }

    public static void main(String[] args) {
        BpmnGenerationExecutionModel.create("current-il", "request");
    }
}
