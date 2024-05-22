package org.rj.modelgen.llm.intrep;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;

public class IntermediateModelParser<TModel extends IntermediateModel> {
    private final Class<? extends TModel> modelClass;

    public IntermediateModelParser(Class<? extends TModel> modelClass) {
        this.modelClass = modelClass;
    }

    public Result<TModel, String> parse(String content) {
        if (StringUtils.isBlank(content)) return Result.Err("Cannot build intermediate model from null content");

        // Simply parse from JSON to object model
        try {
            return Result.Ok(Util.getObjectMapper().readValue(content, modelClass));
        }
        catch (Exception ex) {
            return Result.Err(String.format("Cannot build IR model; content does not conform to required IR schema (%s)", ex.getMessage()));
        }
    }
}
