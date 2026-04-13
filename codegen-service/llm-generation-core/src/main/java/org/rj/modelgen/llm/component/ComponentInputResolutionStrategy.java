package org.rj.modelgen.llm.component;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines how an input should be resolved when the user's runbook does not explicitly specify a value.
 */
public enum ComponentInputResolutionStrategy {

    // LLM inferred a value from runbook context or defaulted; no confirmation needed.
    INFERRED,

    // LLM proposed a value but it should be reviewed by the user.
    INFERRED_CONFIRM,

    //Value could not be determined. User must supply it.
    USER_REQUIRED;

    @JsonValue
    @Override
    public String toString() {
        return name();
    }

    public Boolean requiresUserInvolvement() {
        return this == INFERRED_CONFIRM || this == USER_REQUIRED;
    }
}
