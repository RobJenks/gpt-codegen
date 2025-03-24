package org.rj.modelgen.llm.validation.impl;

import org.rj.modelgen.llm.intrep.noop.NoOpIntermediateModel;

/**
 * No-op implementation which performs no custom sanitization
 */
public class NoOpIntermediateModelSanitizer extends IntermediateModelSanitizer<NoOpIntermediateModel> {
    public NoOpIntermediateModelSanitizer() {
        super(NoOpIntermediateModel.class);
    }

    @Override
    protected String performCustomSanitization(String content) {
        // No-op implementation uses only the built-in operations, no custom sanitization
        return content;
    }

    @Override
    protected NoOpIntermediateModel performCustomModelSanitization(NoOpIntermediateModel model) {
        // No-op implementation uses only the built-in operations, no custom sanitization
        return model;
    }
}
