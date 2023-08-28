package org.rj.modelgen.bpmn.models.generation;

import org.rj.modelgen.bpmn.models.generation.states.BpmnGenerationComplete;
import org.rj.modelgen.bpmn.models.generation.states.SubmitBpmnGenerationRequestToLlm;
import org.rj.modelgen.llm.state.ModelInterfaceExecutionResult;

import java.util.List;
import java.util.Optional;

public class BpmnGenerationResult {
    private final boolean successful;
    private final String generatedBpmn;
    private final List<String> modelValidationMessages;
    private final List<String> bpmnValidationMessages;
    private final ModelInterfaceExecutionResult executionResults;

    public static BpmnGenerationResult fromModelExecutionResult(ModelInterfaceExecutionResult result) {
        final var successResult = Optional.ofNullable(result).map(ModelInterfaceExecutionResult::getResult)
                .flatMap(state -> state.getAs(BpmnGenerationComplete.class));

        return successResult.map(res ->
            new BpmnGenerationResult(true, res.getGeneratedBpmn(), res.getModelValidationMessages(), res.getBpmnValidationMessages(), result)
        ).orElseGet(() ->
            new BpmnGenerationResult(false, null, null, null, result)
        );
    }

    private BpmnGenerationResult(boolean successful, String generatedBpmn, List<String> modelValidationMessages,
                                 List<String> bpmnValidationMessages, ModelInterfaceExecutionResult executionResults) {
        this.successful = successful;
        this.generatedBpmn = generatedBpmn;
        this.modelValidationMessages = modelValidationMessages;
        this.bpmnValidationMessages = bpmnValidationMessages;
        this.executionResults = executionResults;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getGeneratedBpmn() {
        return generatedBpmn;
    }

    public List<String> getModelValidationMessages() {
        return modelValidationMessages;
    }

    public List<String> getBpmnValidationMessages() {
        return bpmnValidationMessages;
    }

    public ModelInterfaceExecutionResult getExecutionResults() {
        return executionResults;
    }
}
