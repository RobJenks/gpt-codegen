package org.rj.modelgen.service;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModel;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModelOptions;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.llm.beans.Prompt;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelInterface;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.Util;
import org.rj.modelgen.service.beans.BpmnGenerationPrompt;
import org.rj.modelgen.service.beans.BpmnGenerationSessionData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.rj.modelgen.llm.util.FuncUtil.*;

@SpringBootApplication
@ComponentScan(basePackages = "org.rj")
@RestController
public class CodegenServiceApplication {
	private final ConcurrentMap<String, BpmnGenerationSessionData> sessions;
	private final BpmnGenerationExecutionModel bpmnGenerationModel;

	public CodegenServiceApplication() {
		this.sessions = new ConcurrentHashMap<>();
		this.bpmnGenerationModel = buildModel();
	}

	private BpmnGenerationExecutionModel buildModel() {
		final var modelInterface = new OpenAIModelInterface.Builder()
				.withApiKeyGenerator(() -> Util.loadStringResource("path/to/api-key"))
				.build();

		final var modelSchema = new ModelSchema(Util.loadStringResource("content/bpmn-intermediate-schema.json"));

		return BpmnGenerationExecutionModel.create(modelInterface, modelSchema, BpmnGenerationExecutionModelOptions.defaultOptions());
	}



	@GetMapping("/api/bpmn/generation/session/{id}")
	public BpmnGenerationSessionData getSessionData(
			@PathVariable("id") String id
	) {
		return getSession(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No session exists with that ID"));
	}

	@PostMapping("/api/bpmn/generation/session/{id}/prompt")
	public Mono<BpmnGenerationSessionData> prompt(
			@PathVariable("id") String id,
			@RequestBody BpmnGenerationPrompt prompt
	) {
		return bpmnGenerationModel.executeModel(id, prompt.getPrompt())
				.doOnSuccess(result -> {
					System.out.println("Result.success = " + result.isSuccessful());
					System.out.println("Result.generated = " + Bpmn.convertToString(result.getGeneratedBpmn()));
					System.out.println("Result.bpmnValidation = " + String.join(", ", result.getBpmnValidationMessages()));
				})
				.map(BpmnGenerationResult::getGeneratedBpmn)
				.map(generatedBpmn -> doVoid(generatedBpmn, bpmn -> getOrCreateSession(id).setCurrentBpmnData(Bpmn.convertToString(bpmn))))
				.map(__ -> getSession(id).orElseThrow());
	}

	private Optional<BpmnGenerationSessionData> getSession(String id) {
		return Optional.ofNullable(sessions.getOrDefault(id, null));
	}

	private BpmnGenerationSessionData getOrCreateSession(String id) {
		return sessions.computeIfAbsent(id, BpmnGenerationSessionData::new);
	}

	public static void main(String[] args) {
		SpringApplication.run(CodegenServiceApplication.class, args);
	}
}
