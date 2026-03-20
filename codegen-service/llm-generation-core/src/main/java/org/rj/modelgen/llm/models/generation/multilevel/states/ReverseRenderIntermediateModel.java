package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.intrep.ModelParser;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.util.Result;
import reactor.core.publisher.Mono;

public class ReverseRenderIntermediateModel<TModel, TIntermediateModel extends IntermediateModel> extends ModelInterfaceState {

    private final ModelParser<TModel> modelParser;
    private final ReverseRenderFunction<TModel, TIntermediateModel> reverseRenderFunction;

    public ReverseRenderIntermediateModel(ModelParser<TModel> modelParser,
                                          ReverseRenderFunction<TModel, TIntermediateModel> reverseRenderFunction) {
        this(ReverseRenderIntermediateModel.class, modelParser, reverseRenderFunction);
    }

    public ReverseRenderIntermediateModel(Class<? extends ReverseRenderIntermediateModel> cls,
                                          ModelParser<TModel> modelParser,
                                          ReverseRenderFunction<TModel, TIntermediateModel> reverseRenderFunction) {
        super(cls);
        this.modelParser = modelParser;
        this.reverseRenderFunction = reverseRenderFunction;
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        String inputKey = StandardModelData.CanvasModel.toString();
        final String inputContent = getPayload().get(inputKey);

        if (inputContent == null || inputContent.isEmpty()) {
            return error(String.format("No input content found at payload key '%s'", getPayload().get(inputKey)));
        }

        final var model = modelParser.parse(inputContent);
        if (model.isErr()) {
            return error(String.format("Failed parsing model (%s)", model.getError()));
        }

        final var generatedIntermediateModel = reverseRenderModel(model.getValue());
        if (generatedIntermediateModel.isErr()) {
            final var errorMsg = String.format("Failed to generate intermediate model from model data (%s)",
                    generatedIntermediateModel.getError());
            recordAudit("reverse-render", errorMsg);
            return error(errorMsg);
        }

        final String serializedIntermediateModel = generatedIntermediateModel.getValue().serialize();
        recordAudit("reverse-render", serializedIntermediateModel);

        return outboundSignal(getSuccessSignalId())
                .withPayloadData(MultiLevelModelStandardPayloadData.Model, model.getValue())
                .withPayloadData(MultiLevelModelStandardPayloadData.ReverseRenderedIntermediateModel.toString(), generatedIntermediateModel.getValue())
                .withPayloadData(MultiLevelModelStandardPayloadData.SerializedReverseRender.toString(), serializedIntermediateModel)
                .mono();
    }

    protected Result<TIntermediateModel, String> reverseRenderModel(TModel model) {
        recordAudit("reverse-render-input-model", model.toString());
        return reverseRenderFunction.reverseRenderModelToIR(model);
    }

    @Override
    public String getDescription() {
        return "Reverse Render the given model into its intermediate representation";
    }
}
