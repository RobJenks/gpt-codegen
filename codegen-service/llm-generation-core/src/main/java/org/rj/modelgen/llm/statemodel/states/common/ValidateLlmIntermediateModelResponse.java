package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.validation.IntermediateModelValidationProvider;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class ValidateLlmIntermediateModelResponse
        extends ModelInterfaceState implements CommonStateInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateLlmIntermediateModelResponse.class);

    private final ModelSchema modelSchema;
    private final IntermediateModelValidationProvider<? extends IntermediateModel> validationProvider;
    private String modelInputKey;


    public ValidateLlmIntermediateModelResponse(ModelSchema modelSchema, Class<? extends IntermediateModel> modelClass) {
        this(ValidateLlmIntermediateModelResponse.class, modelSchema, modelClass);
    }

    public ValidateLlmIntermediateModelResponse(Class<? extends ValidateLlmIntermediateModelResponse> cls,
                                                ModelSchema modelSchema, Class<? extends IntermediateModel> modelClass) {
        super(cls);
        this.modelSchema = modelSchema;
        this.validationProvider = new IntermediateModelValidationProvider<>(modelSchema, modelClass);
    }

    @Override
    public String getDescription() {
        return "Validating intermediate representation returned by LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {

        // Fail immediately in case of LLM-reported errors
        final ModelResponse response = getPayload().get(StandardModelData.ModelResponse);
        if (!response.isSuccessful()) {
            return error(String.format("LLM model execution ended in failure (%s)", response.getError()));
        }

        // Perform validation
        final String modelContent = getPayload().get(getModelInputKey());
        final var errors = validationProvider.validate(modelContent);
        if (errors.hasErrors()) {
            return error(String.format("LLM intermediate model response failed validation (%s)",
                    errors.getErrors().stream().map(IntermediateModelValidationError::toString).collect(Collectors.joining("; "))));
        }

        final String sessionId = getPayload().get(StandardModelData.SessionId);
        LOG.info("Session {} intermediate model response passed validations", sessionId);

        return outboundSignal(getSuccessSignalId())
                .withPayloadData(StandardModelData.ValidationMessages, List.of())   // TODO: Record validation errors
                .mono();
    }

    public ValidateLlmIntermediateModelResponse withModelInputKey(String inputKey) {
        setModelInputKey(inputKey);
        return this;
    }
    public<T extends Enum<T>> ValidateLlmIntermediateModelResponse withModelInputKey(T inputKey) {
        setModelInputKey(inputKey.toString());
        return this;
    }

    public void setModelInputKey(String inputKey) {
        this.modelInputKey = inputKey;
    }

    // Input key can be explicitly provided to control which input data is validated.  If not provided, this node
    // will validate the last model response received by default
    private String getModelInputKey() {
        if (modelInputKey == null) {
            return StandardModelData.ResponseContent.toString();
        }

        return modelInputKey;
    }
}
