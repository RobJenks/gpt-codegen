package org.rj.modelgen.llm.schema;

import org.rj.modelgen.llm.schema.model.IntermediateModel;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;

public class IntermediateModelParser {
    public Result<IntermediateModel, String> parse(String content) {
        // Simply parse from JSON to object model
        try {
            return Result.Ok(Util.getObjectMapper().readValue(content, IntermediateModel.class));
        }
        catch (Exception ex) {
            return Result.Err("Response does not conform to required schema");
        }
    }
}
