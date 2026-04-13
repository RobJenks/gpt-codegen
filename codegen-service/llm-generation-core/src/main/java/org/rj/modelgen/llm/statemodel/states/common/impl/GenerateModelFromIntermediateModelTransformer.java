package org.rj.modelgen.llm.statemodel.states.common.impl;

import org.rj.modelgen.llm.generation.ModelGenerationFunction;
import org.rj.modelgen.llm.intrep.assets.IntermediateModelAssets;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.statemodel.states.common.GenerateModelFromIntermediateModel;
import org.rj.modelgen.llm.util.Result;

import java.util.function.Function;

public class GenerateModelFromIntermediateModelTransformer<TIntermediateModel extends IntermediateModel, TIntermediateModelAssets extends IntermediateModelAssets, TModel>
        extends GenerateModelFromIntermediateModel<TIntermediateModel, TIntermediateModelAssets, TModel> {

    private final ModelGenerationFunction<TIntermediateModel, TModel> generationFunction;

    public GenerateModelFromIntermediateModelTransformer(Class<? extends TIntermediateModel> intermediateModelClass,
                                                         String inputModelKey, String outputModelKey,
                                                         ModelGenerationFunction<TIntermediateModel, TModel> generationFunction,
                                                         Function<TModel, String> renderedModelSerializer) {
        this(GenerateModelFromIntermediateModelTransformer.class, intermediateModelClass, inputModelKey, outputModelKey, generationFunction, renderedModelSerializer);
    }

    public GenerateModelFromIntermediateModelTransformer(Class<? extends GenerateModelFromIntermediateModelTransformer> cls,
                                                         Class<? extends TIntermediateModel> intermediateModelClass,
                                                         String inputModelKey, String outputModelKey,
                                                         ModelGenerationFunction<TIntermediateModel, TModel> generationFunction,
                                                         Function<TModel, String> renderedModelSerializer) {
        super(cls, intermediateModelClass, inputModelKey, outputModelKey, renderedModelSerializer);
        this.generationFunction = generationFunction;
    }

    @Override
    protected Result<TModel, String> generateModel(TIntermediateModel intermediateModel, ModelInterfaceStateMachine executionModel) {
        recordAudit("prerender", intermediateModel.serialize());

        return generationFunction.generateModel(intermediateModel, executionModel);
    }
}
