package org.rj.modelgen.llm.validation.generic;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

/**
 * No-op implementation which performs no custom sanitization
 */
public class NoOpIntermediateModelSanitizer extends IntermediateModelSanitizer<IntermediateModel> {
    public NoOpIntermediateModelSanitizer() {
        super(IntermediateModel.class);
    }

    @Override
    protected String performCustomSanitization(String content) {
        // No-op implementation uses only the built-in operations, no custom sanitization
        return content;
    }

    @Override
    protected IntermediateModel performCustomModelSanitization(IntermediateModel model) {
        // No-op implementation uses only the built-in operations, no custom sanitization
        return model;
    }
}
