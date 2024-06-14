package org.rj.modelgen.bpmn.models.generation;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface BpmnGenerationExecutionModel {

    Mono<BpmnGenerationResult> executeModel(String sessionId, String request, Map<String, Object> data);

}
