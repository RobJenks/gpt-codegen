package org.rj.modelgen.bpmn.models.generation.base.signals;

public enum BpmnGenerationSignals {
    StartBpmnGeneration,
    PrepareLlmRequest,
    SubmitRequestToLlm,
    ValidateLlmResponse,
    GenerateBpmnXmlFromLlmResponse,
    ValidateBpmnXml,
    CompleteGeneration
}
