package org.rj.modelgen.bpmn.models.generation.base.signals;

public enum BpmnGenerationSignals {
    StartBpmnGeneration,
    PrepareLlmRequest,
    SubmitRequestToLlm,
    ValidateLlmResponse,
    IntermediateModelIsInvalid,
    IntermediateModelIsValid,
    GenerateBpmnXmlFromLlmResponse,
    ValidateBpmnXml,
    CompleteGeneration
}
