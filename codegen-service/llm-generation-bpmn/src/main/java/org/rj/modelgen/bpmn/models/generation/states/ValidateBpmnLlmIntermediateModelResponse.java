package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;

public class ValidateBpmnLlmIntermediateModelResponse extends ValidateLlmIntermediateModelResponse {

    public ValidateBpmnLlmIntermediateModelResponse(ModelSchema modelSchema, Class<? extends IntermediateModel> modelClass) {
        super(ValidateBpmnLlmIntermediateModelResponse.class, modelSchema, modelClass);
    }

    @Override
    public String getSuccessSignalId() {
        return BpmnGenerationSignals.GenerateBpmnXmlFromLlmResponse.toString();
    }
}
