package org.rj.modelgen.bpmn.models.generation;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.base.states.BpmnGenerationComplete;
import org.rj.modelgen.llm.models.generation.GenerationResult;
import org.rj.modelgen.llm.state.ModelInterfaceExecutionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BpmnGenerationResult extends GenerationResult {
    private final boolean successful;
    private final BpmnIntermediateModel intermediateModel;
    private final BpmnModelInstance generatedBpmn;
    private final List<String> bpmnValidationMessages;
    private final ModelInterfaceExecutionResult executionResults;

    public static BpmnGenerationResult fromModelExecutionResult(ModelInterfaceExecutionResult result) {
        final var successResult = Optional.ofNullable(result).map(ModelInterfaceExecutionResult::getResult)
                .flatMap(state -> state.getAs(BpmnGenerationComplete.class));

        return successResult.map(res ->
            new BpmnGenerationResult(true, res.getIntermediateModel(), res.getGeneratedBpmn(), res.getBpmnValidationMessages(), result)
        ).orElseGet(() ->
            new BpmnGenerationResult(false, null, null, null, result)
        );
    }

    private BpmnGenerationResult(boolean successful, BpmnIntermediateModel intermediateModel, BpmnModelInstance generatedBpmn,
                                 List<String> bpmnValidationMessages, ModelInterfaceExecutionResult executionResults) {
        this.successful = successful;
        this.intermediateModel = intermediateModel;
        this.generatedBpmn = generatedBpmn;
        this.bpmnValidationMessages = Optional.ofNullable(bpmnValidationMessages).orElseGet(ArrayList::new);
        this.executionResults = executionResults;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public BpmnIntermediateModel getIntermediateModel() {
        return intermediateModel;
    }

    public BpmnModelInstance getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }

    public ModelInterfaceExecutionResult getExecutionResults() {
        return executionResults;
    }
}
