package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.validation.IntermediateModelValidationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class ValidateLlmIntermediateModelResponse extends ModelInterfaceState {
    private static final Logger LOG = LoggerFactory.getLogger(ValidateLlmIntermediateModelResponse.class);

    private final ModelSchema modelSchema;
    private final IntermediateModelValidationProvider<? extends IntermediateModel> validationProvider;

    public ValidateLlmIntermediateModelResponse(ModelSchema modelSchema, Class<? extends IntermediateModel> modelClass) {
        super(ValidateLlmIntermediateModelResponse.class);
        this.modelSchema = modelSchema;
        this.validationProvider = new IntermediateModelValidationProvider<>(modelSchema, modelClass);
    }

    @Override
    public String getDescription() {
        return "Clean up and validate intermediate representation returned by LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        // Fail immediately in case of LLM-reported errors
        if (!input.getModelResponse().isSuccessful()) {
            // TODO: Generate error signal
        }

        // Perform validation
        final var errors = validationProvider.validate(input.getSanitizedResponseContent());
        if (errors.hasErrors()) {
            // TODO: Generate error signal
        }

        LOG.info("Session {} intermediate model response passed validations", input.getSessionId());
        return outboundSignal(new LlmResponseModelDataIsValid(input.getSessionId(), input.getModelResponse(), input.getSanitizedResponseContent(), List.of()));
    }
}
