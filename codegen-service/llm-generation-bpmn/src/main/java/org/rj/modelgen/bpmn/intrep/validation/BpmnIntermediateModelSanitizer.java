package org.rj.modelgen.bpmn.intrep.validation;

import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class BpmnIntermediateModelSanitizer extends IntermediateModelSanitizer {
    public BpmnIntermediateModelSanitizer() {
        super();
    }

    @Override
    protected String performCustomSanitization(String content) {
        return content;
    }
}
