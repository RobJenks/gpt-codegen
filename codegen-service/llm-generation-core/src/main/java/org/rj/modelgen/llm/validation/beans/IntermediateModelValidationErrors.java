package org.rj.modelgen.llm.validation.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntermediateModelValidationErrors {
    private List<IntermediateModelValidationError> errors;

    public IntermediateModelValidationErrors() {
        this(List.of());
    }

    public IntermediateModelValidationErrors(IntermediateModelValidationError error) {
        this(Stream.of(error).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public IntermediateModelValidationErrors(List<IntermediateModelValidationError> errors) {
        this.errors = Optional.ofNullable(errors).map(ArrayList::new).orElseGet(ArrayList::new);
    }

    public static IntermediateModelValidationErrors empty(){
        return new IntermediateModelValidationErrors();
    }

    public static IntermediateModelValidationErrors singleMessage(String errorMessage){
        return new IntermediateModelValidationErrors(new IntermediateModelValidationError(errorMessage, null));
    }

    public List<IntermediateModelValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<IntermediateModelValidationError> errors) {
        this.errors = errors;
    }

    public void addError(IntermediateModelValidationError error) {
        if (error != null) {
            this.errors.add(error);
        }
    }

    public void addErrors(List<IntermediateModelValidationError> errors) {
        if (errors == null) return;
        errors.stream()
                .filter(Objects::nonNull)
                .forEach(this::addError);
    }

    public IntermediateModelValidationErrors withError(IntermediateModelValidationError error) {
        addError(error);
        return this;
    }

    public IntermediateModelValidationErrors withErrors(List<IntermediateModelValidationError> errors) {
        addErrors(errors);
        return this;
    }

    @JsonIgnore
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @JsonIgnore
    public boolean isSuccess() {
        return !hasErrors();
    }
}
