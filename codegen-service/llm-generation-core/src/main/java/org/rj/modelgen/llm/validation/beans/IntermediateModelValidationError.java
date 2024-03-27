package org.rj.modelgen.llm.validation.beans;

public class IntermediateModelValidationError {
    private String error;
    private String location;

    public IntermediateModelValidationError() {
        this(null);
    }

    public IntermediateModelValidationError(String error) {
        this(error, null);
    }

    public IntermediateModelValidationError(String error, String location) {
        this.error = error;
        this.location = location;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("%s (at '%s')", error, location);
    }
}
