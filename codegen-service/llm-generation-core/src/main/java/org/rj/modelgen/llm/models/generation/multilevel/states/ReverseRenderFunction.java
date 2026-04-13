package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.util.Result;

public interface ReverseRenderFunction<TModel, TIntermediateModel extends IntermediateModel> {
    Result<TIntermediateModel, String> reverseRenderModelToIR(TModel model);
}
