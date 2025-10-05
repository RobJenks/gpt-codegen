package org.rj.modelgen.bpmn.intrep.validation;

import org.rj.modelgen.bpmn.intrep.model.BpmnHighLevelIntermediateModel;
import org.rj.modelgen.llm.validation.impl.IntermediateModelSanitizer;

import java.util.ArrayList;

public class BpmnHighLevelIntermediateModelSanitizer extends IntermediateModelSanitizer<BpmnHighLevelIntermediateModel> {
    public BpmnHighLevelIntermediateModelSanitizer() {
        super(BpmnHighLevelIntermediateModel.class);
    }

    @Override
    protected String performCustomSanitization(String content) {
        return content;
    }

    @Override
    protected BpmnHighLevelIntermediateModel performCustomModelSanitization(BpmnHighLevelIntermediateModel model) {
        model.getNodes().forEach(node -> {
            if (node.getInputs() == null) {
                node.setInputs(new ArrayList<>());
            }
            if (node.getConnectedTo() == null) {
                node.setConnectedTo(new ArrayList<>());
            }
        });
        return model;
    }
}
