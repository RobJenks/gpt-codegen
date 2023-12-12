package org.rj.modelgen.llm.exception;

import java.io.Serial;

public class LlmGenerationModelException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1;

    public LlmGenerationModelException(String message) {
        super(message);
    }

    public LlmGenerationModelException(Throwable cause) {
        super(cause);
    }

    public LlmGenerationModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
