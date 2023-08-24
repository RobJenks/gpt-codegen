package org.rj.modelgen.bpmn.execution.model;

import org.rj.modelgen.bpmn.execution.signal.PrepareLlmRequestSignal;
import org.rj.modelgen.bpmn.execution.signal.RequestSubmissionToLlmSignal;
import org.rj.modelgen.bpmn.execution.state.PrepareBpmnModelGenerationRequest;
import org.rj.modelgen.bpmn.execution.state.StartBpmnGeneration;
import org.rj.modelgen.bpmn.execution.state.SubmitBpmnGenerationRequestToLlm;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRule;
import org.rj.modelgen.llm.state.ModelInterfaceTransitionRules;

import java.util.List;

public class BpmnGenerationExecutionModel {

    public static ModelInterfaceStateMachine create(String currentIL, String request) {
        final var stateInit = new StartBpmnGeneration();
        final var statePrepareRequest = new PrepareBpmnModelGenerationRequest();
        final var stateSubmitToLlm = new SubmitBpmnGenerationRequestToLlm();


        final var rules = new ModelInterfaceTransitionRules(List.of(
                new ModelInterfaceTransitionRule<>(stateInit, new PrepareLlmRequestSignal(), statePrepareRequest),
                new ModelInterfaceTransitionRule<>(statePrepareRequest, new RequestSubmissionToLlmSignal(), stateSubmitToLlm)
        ));



    }

}
