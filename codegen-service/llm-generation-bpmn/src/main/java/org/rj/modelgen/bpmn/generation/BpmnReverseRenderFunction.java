package org.rj.modelgen.bpmn.generation;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.json.JSONObject;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.BpmnReverseRenderer;
import org.rj.modelgen.llm.models.generation.multilevel.states.ReverseRenderFunction;
import org.rj.modelgen.llm.util.Result;

public class BpmnReverseRenderFunction implements ReverseRenderFunction<BpmnModelInstance, BpmnIntermediateModel> {

    public BpmnReverseRenderFunction() {

    }

    @Override
    public Result<BpmnIntermediateModel, String> reverseRenderModelToIR(BpmnModelInstance model) {
        try {
            final var reverseRenderer = new BpmnReverseRenderer(model);
            final BpmnIntermediateModel rendered = reverseRenderer.generateBpmnIntermediateModel();

            return Result.Ok(rendered);
        }
        catch (Throwable t) {
            return Result.Err("Could not reverse render intermediate model from input BPMN model: " + t.getMessage());
        }
    }

}