package org.rj.modelgen.llm.exception;

import java.io.Serial;

public class LlmGenerationConfigException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1;

    public LlmGenerationConfigException(String message) {
        super(message);
    }

    public LlmGenerationConfigException(Throwable cause) {
        super(cause);
    }

    public LlmGenerationConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
