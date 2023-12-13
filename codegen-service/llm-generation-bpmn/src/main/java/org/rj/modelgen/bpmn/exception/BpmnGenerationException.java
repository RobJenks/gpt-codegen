package org.rj.modelgen.bpmn.exception;

import java.io.Serial;

public class BpmnGenerationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1;

    public BpmnGenerationException(String message) {
        super(message);
    }

    public BpmnGenerationException(Throwable cause) {
        super(cause);
    }

    public BpmnGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
