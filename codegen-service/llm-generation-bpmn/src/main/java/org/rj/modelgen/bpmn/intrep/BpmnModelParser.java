package org.rj.modelgen.bpmn.intrep;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.json.JSONObject;
import org.rj.modelgen.llm.intrep.ModelParser;
import org.rj.modelgen.llm.util.Result;

public class BpmnModelParser extends ModelParser<BpmnModelInstance> {

    public BpmnModelParser() {}

    public Result<BpmnModelInstance, String> parse(String content) {
        if (StringUtils.isBlank(content)) {
            return Result.Err("Cannot build model from null content");
        } else {
            try {
                return Result.Ok();
            } catch (Exception ex) {
                return Result.Err(String.format("Cannot build model; content does not conform to JSONObject standard (%s)", ex.getMessage()));
            }
        }
    }
}
