package org.rj.modelgen.llm.models.generation.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.subproblem.data.SubproblemDecompositionPayloadData;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class OverriddenLlmResponse {
    private final String response;
    private final ModelResponse.Status status;
    private final Predicate<ModelInterfacePayload> condition;

    public OverriddenLlmResponse(String response, ModelResponse.Status status) {
        this(response, status, StandardConditions.NO_CONDITION);
    }

    public OverriddenLlmResponse(String response, ModelResponse.Status status, Predicate<ModelInterfacePayload> condition) {
        this.response = response;
        this.status = status;
        this.condition = condition;
    }

    public String getResponse() {
        return response;
    }

    public ModelResponse.Status getStatus() {
        return status;
    }

    public Predicate<ModelInterfacePayload> getCondition() {
        return condition;
    }

    @JsonIgnore
    public boolean isApplicable(ModelInterfacePayload modelPayload) {
        return Optional.ofNullable(condition).orElse(StandardConditions.NO_CONDITION)
                .test(modelPayload);
    }

    @JsonIgnore
    public ModelResponse generateModelResponse() {
        return switch (this.status) {
            case SUCCESS -> generateModelSuccessResponse();
            case FAILED -> generateModelFailureResponse();
        };
    }

    private ModelResponse generateModelSuccessResponse() {
        final var response = new ModelResponse();
        response.setStatus(ModelResponse.Status.SUCCESS);
        response.setMessage(this.response);

        return response;
    }

    private ModelResponse generateModelFailureResponse() {
        final var response = new ModelResponse();
        response.setStatus(ModelResponse.Status.FAILED);
        response.setError(this.response);

        return response;
    }

    public static class StandardConditions {
        public static final Predicate<ModelInterfacePayload> NO_CONDITION = (__ -> true);
        public static Predicate<ModelInterfacePayload> FOR_SUBPROBLEM_ID(int subproblemId) {
            return (
                    payload -> payload != null &&
                            payload.<Integer>getOrElse(SubproblemDecompositionPayloadData.CurrentSubproblem.toString(), -999) == subproblemId
                   );
        }
    }
}
