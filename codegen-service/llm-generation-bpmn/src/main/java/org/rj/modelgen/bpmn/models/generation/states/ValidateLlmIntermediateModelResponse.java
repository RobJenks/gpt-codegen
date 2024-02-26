package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnGenerationSignals;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseReceived;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
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
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {

        // Fail immediately in case of LLM-reported errors
        final ModelResponse response = getPayload().get(StandardModelData.ModelResponse);
        if (!response.isSuccessful()) {
            // TODO: Generate error signal
        }

        // Perform validation
        final String sanitizedContent = getPayload().get(StandardModelData.SanitizedContent);
        final var errors = validationProvider.validate(sanitizedContent);
        if (errors.hasErrors()) {
            // TODO: Generate error signal
        }

        final String sessionId = getPayload().get(StandardModelData.SessionId);
        LOG.info("Session {} intermediate model response passed validations", sessionId);

        return outboundSignal(BpmnGenerationSignals.GenerateBpmnXmlFromLlmResponse)
                .withPayloadData(StandardModelData.ValidationMessages, List.of())   // TODO: Record validation errors
                .mono();
    }
}
