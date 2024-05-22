package org.rj.modelgen.bpmn.generation;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.util.Result;

public class BpmnModelGenerationFunction implements ModelGenerationFunction<BpmnIntermediateModel, BpmnModelInstance> {
    final BasicBpmnModelGenerator modelGenerator;

    public BpmnModelGenerationFunction() {
        this.modelGenerator = new BasicBpmnModelGenerator();
    }

    @Override
    public Result<BpmnModelInstance, String> generateModel(BpmnIntermediateModel intermediateModel) {
        return modelGenerator.generateModel(intermediateModel);
    }
}
