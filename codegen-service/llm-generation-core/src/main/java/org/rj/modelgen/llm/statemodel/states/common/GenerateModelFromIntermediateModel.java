package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.util.Result;
import reactor.core.publisher.Mono;

public abstract class GenerateModelFromIntermediateModel<TIntermediateModel extends IntermediateModel, TModel> extends ModelInterfaceState {
    private final IntermediateModelParser<TIntermediateModel> intermediateModelParser;
    private final String inputModelKey;
    private final String outputModelKey;

    public GenerateModelFromIntermediateModel(Class<? extends TIntermediateModel> intermediateModelClass, String inputModelKey, String outputModelKey) {
        this(GenerateModelFromIntermediateModel.class, intermediateModelClass, inputModelKey, outputModelKey);
    }

    public GenerateModelFromIntermediateModel(Class<? extends GenerateModelFromIntermediateModel> cls,
                                              Class<? extends TIntermediateModel> intermediateModelClass, String inputModelKey, String outputModelKey) {
        super(cls);
        this.intermediateModelParser = new IntermediateModelParser<>(intermediateModelClass);
        this.inputModelKey = inputModelKey;
        this.outputModelKey = outputModelKey;
    }

    @Override
    public String getDescription() {
        return "Generate a model from the current intermediate model representation";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final String inputContent = getPayload().get(inputModelKey);
        final var intermediateModel = intermediateModelParser.parse(inputContent);
        if (intermediateModel.isErr()) {
            return error(String.format("LLM response failed intermediate model parsing (%s)", intermediateModel.getError()));
        }

        final var generatedModel = generateModel(intermediateModel.getValue());
        if (generatedModel.isErr()) {
            return error(String.format("Failed to generate model from intermediate model data (%s)", generatedModel.getError()));
        }

        return outboundSignal(getSuccessSignalId())
                .withPayloadData(StandardModelData.IntermediateModel, intermediateModel.getValue())
                .withPayloadData(outputModelKey, generatedModel.getValue())
                .mono();
    }

    /**
     * Implemented by subclasses; generate the full model from an intermediate model representation
     *
     * @param intermediateModel     Intermediate model representation
     * @return                      Generated model, or Result.Err in case of generation failure
     */
    protected abstract Result<TModel, String> generateModel(TIntermediateModel intermediateModel);

}
