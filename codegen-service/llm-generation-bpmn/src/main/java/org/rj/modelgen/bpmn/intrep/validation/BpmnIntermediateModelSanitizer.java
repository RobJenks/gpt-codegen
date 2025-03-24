package org.rj.modelgen.bpmn.intrep.validation;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.llm.validation.impl.IntermediateModelSanitizer;

import java.util.ArrayList;
import java.util.Optional;

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
        return Optional.ofNullable(model)
                .map(this::normalizeConnections)
                .orElse(null);
    }

    private BpmnIntermediateModel normalizeConnections(BpmnIntermediateModel model) {
        // Normalize any null connection sets to an empty array, to simplify schema presented to the model
        for (final var node : model.getNodes()) {
            if (node.getConnectedTo() == null) {
                node.setConnectedTo(new ArrayList<>());
            }
        }

        return model;
    }
}
