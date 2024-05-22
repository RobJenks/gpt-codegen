package org.rj.modelgen.service;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.rj.modelgen.bpmn.intrep.schema.BpmnIntermediateModelSchema;
import org.rj.modelgen.bpmn.models.generation.base.BpmnGenerationExecutionModel;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModelOptions;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.bpmn.models.generation.multilevel.BpmnMultiLevelGenerationModel;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelInterface;
import org.rj.modelgen.llm.util.Util;
import org.rj.modelgen.service.beans.BpmnGenerationPrompt;
import org.rj.modelgen.service.beans.BpmnGenerationSessionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.rj.modelgen.llm.util.FuncUtil.*;

@SpringBootApplication
@ComponentScan(basePackages = "org.rj")
@RestController
public class CodegenServiceApplication {
	private final ConcurrentMap<String, BpmnGenerationSessionData> sessions;
	//private final BpmnGenerationExecutionModel bpmnGenerationModel;
	private final BpmnMultiLevelGenerationModel bpmnGenerationModel;

	@Value("${app.tokenPath}")
	private String tokenPath;

	public CodegenServiceApplication() {
		this.sessions = new ConcurrentHashMap<>();
		this.bpmnGenerationModel = buildMultiPhaseModel(); // buildModel();
	}

	private BpmnGenerationExecutionModel buildModel() {
		final var modelInterface = new OpenAIModelInterface.Builder()
				.withApiKeyGenerator(() -> Util.loadStringResource(tokenPath))
				.build();

		final var modelSchema = new BpmnIntermediateModelSchema();

		return BpmnGenerationExecutionModel.create(modelInterface, modelSchema, BpmnGenerationExecutionModelOptions.defaultOptions());
	}

	private BpmnMultiLevelGenerationModel buildMultiPhaseModel() {
		final var modelInterface = new OpenAIModelInterface.Builder()
				.withApiKeyGenerator(() -> Util.loadStringResource(tokenPath))
				.build();

		return BpmnMultiLevelGenerationModel.create(modelInterface);
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
