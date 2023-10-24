package org.rj.modelgen.service;

import org.rj.modelgen.bpmn.models.generation.BpmnGenerationExecutionModel;
import org.rj.modelgen.bpmn.models.generation.BpmnGenerationResult;
import org.rj.modelgen.llm.client.LlmClientImpl;
import org.rj.modelgen.llm.integrations.openai.OpenAIClientConfig;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelInterface;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@ComponentScan(basePackages = "org.rj")
@RestController
public class CodegenServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodegenServiceApplication.class, args);
	}

	@GetMapping("/echo/{value}")
	public String echo(
			@PathVariable(name="value") String value
	) {
		return value;
	}

	@GetMapping("/test")
	public Mono<String> test() {
		final var modelInterface = new OpenAIModelInterface.Builder()
				.withApiKeyGenerator(() -> Util.loadStringResource("k"))
				.build();

		final var modelSchema = new ModelSchema(Util.loadStringResource("content/bpmn-intermediate-schema.json"));
		final var model = BpmnGenerationExecutionModel.create(modelInterface, modelSchema);

		return model.executeModel("123", "Create a basic approval process")
				.doOnSuccess(result -> {
					System.out.println("Result.success = " + result.isSuccessful());
					System.out.println("Result.generated = " + result.getGeneratedBpmn());
					System.out.println("Result.modelValidation = " + String.join(", ", result.getModelValidationMessages()));
					System.out.println("Result.bpmnValidation = " + String.join(", ", result.getBpmnValidationMessages()));
				})
				.map(BpmnGenerationResult::getGeneratedBpmn);
	}
}
