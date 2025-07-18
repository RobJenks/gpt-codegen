package org.rj.modelgen.llm.generation;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.util.Result;

public interface ModelGenerationFunction<TIntermediateModel extends IntermediateModel, TModel> {

    Result<TModel, String> generateModel(TIntermediateModel intermediateModel, ModelInterfaceStateMachine executionModel);

}
