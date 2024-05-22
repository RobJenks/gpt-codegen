package org.rj.modelgen.bpmn.intrep.validation;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class BpmnIntermediateModelSanitizer extends IntermediateModelSanitizer<BpmnIntermediateModel> {
    public BpmnIntermediateModelSanitizer() {
        super(BpmnIntermediateModel.class);
    }

    @Override
    protected String performCustomSanitization(String content) {
        return content;
    }

    @Override
    protected BpmnIntermediateModel performCustomModelSanitization(BpmnIntermediateModel model) {
        return model;
    }
}
