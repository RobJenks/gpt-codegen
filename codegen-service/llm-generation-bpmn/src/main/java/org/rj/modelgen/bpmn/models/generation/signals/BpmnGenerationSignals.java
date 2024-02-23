package org.rj.modelgen.bpmn.models.generation.signals;

public enum BpmnGenerationSignals {
    StartBpmnGeneration,
    PrepareLlmRequest,
    SubmitRequestToLlm,
    ValidateLlmResponse,
    GenerateBpmnXmlFromLlmResponse,
    ValidateBpmnXml,
    CompleteGeneration
}
