package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementNode;
import org.rj.modelgen.bpmn.component.synthetic.BpmnSyntheticElementType;
import org.rj.modelgen.bpmn.component.synthetic.config.BpmnSyntheticElementConfig;
import org.rj.modelgen.bpmn.component.synthetic.resolution.BpmnSyntheticElementResolver;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.states.common.ResolveSyntheticComponents;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;

import java.util.Optional;

public class ResolveSyntheticBpmnComponents extends ResolveSyntheticComponents<String, String, ElementConnection, ElementNode, BpmnIntermediateModel,
        BpmnSyntheticElementType, BpmnSyntheticElementNode, BpmnSyntheticElementConfig, BpmnSyntheticElementResolver> {

    public ResolveSyntheticBpmnComponents() {
        super(ResolveSyntheticBpmnComponents.class,
                new BpmnSyntheticElementResolver(BpmnSyntheticElementConfig.defaultConfig()));
    }

    @Override
    protected Result<BpmnIntermediateModel, String> getModelData() {
        return Optional.ofNullable(getPayload().<String, MultiLevelModelStandardPayloadData>get(MultiLevelModelStandardPayloadData.DetailLevelModel))
                .map(serialized -> Util.tryDeserialize(serialized, BpmnIntermediateModel.class))
                .map(res -> res.mapErr(Exception::toString))
                .orElseGet(() -> Result.Err("No valid detail-level model found"));
    }

    @Override
    protected Result<Void, String> setModelData(BpmnIntermediateModel model) {
        final var serialized = Util.trySerialize(model);
        if (serialized.isErr()) {
            return Result.Err("Failed to serialize model after resolving synthetic components: %s".formatted(serialized.getError()));
        }

        getPayload().put(MultiLevelModelStandardPayloadData.DetailLevelModel, serialized.getValue());
        return Result.Ok();
    }

    @Override
    protected boolean isOfType(ElementNode node, String type) {
        return type.equals(node.getElementType());
    }

}

