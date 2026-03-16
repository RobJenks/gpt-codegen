package org.rj.modelgen.llm.intrep.assets;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;

public class IntermediateModelAssetsParser<TModelAssets extends IntermediateModelAssets> {
    private final Class<? extends TModelAssets> modelAssetsClass;

    public IntermediateModelAssetsParser(Class<? extends TModelAssets> modelAssetsClass) {
        this.modelAssetsClass = modelAssetsClass;
    }

    public Result<TModelAssets, String> parse(String content) {
        if (StringUtils.isBlank(content)) return Result.Err("Cannot build intermediate model assets from null content");

        try {
            return Result.Ok(Util.getObjectMapper().readValue(content, modelAssetsClass));
        }
        catch (Exception ex) {
            return Result.Err(String.format("Cannot build IR model assets; content does not conform to required IR schema (%s)", ex.getMessage()));
        }
    }


}
