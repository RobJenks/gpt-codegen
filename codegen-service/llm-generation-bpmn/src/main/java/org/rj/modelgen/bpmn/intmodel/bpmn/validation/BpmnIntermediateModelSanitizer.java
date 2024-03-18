package org.rj.modelgen.bpmn.intmodel.bpmn.validation;

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
