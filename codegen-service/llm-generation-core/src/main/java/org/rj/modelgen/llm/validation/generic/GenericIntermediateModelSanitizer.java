package org.rj.modelgen.llm.validation.generic;

import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class GenericIntermediateModelSanitizer extends IntermediateModelSanitizer {
    public GenericIntermediateModelSanitizer() {
        super();
    }

    @Override
    protected String performCustomSanitization(String content) {
        // Generic implementation uses only the built-in operations
        return content;
    }
}
