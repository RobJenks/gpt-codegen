package org.rj.modelgen.bpmn.generation;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.exception.BpmnGenerationException;
import org.rj.modelgen.bpmn.intrep.bpmn.model.BpmnIntermediateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BulkBpmnModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BulkBpmnModelGenerator.class);

    public List<BpmnModelInstance> convert(List<BpmnIntermediateModel> models, boolean ignoreInvalidModels) {
        if (models == null) throw new BpmnGenerationException("No models provided for bulk-conversion");
        LOG.info("Starting bulk conversion of {} models", models.size());

        final var generator = new BasicBpmnModelGenerator();
        final var results = new ArrayList<BpmnModelInstance>();

        for (final var model : models) {
            if (model == null) {
                if (ignoreInvalidModels) continue;
                throw new BpmnGenerationException("Invalid null model encountered during bulk model conversion");
            }

            final var result = generator.generateModel(model);
            if (result.isErr()) {
                if (!ignoreInvalidModels) throw new BpmnGenerationException("Failed to generate BPMN for intermediate model");
            }
            else {
                results.add(result.getValue());
            }
        }

        LOG.info("Generated {} BPMN models", results.size());
        return results;
    }

}
