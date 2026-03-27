package org.rj.modelgen.llm.models.generation.multilevel.states;

import org.rj.modelgen.llm.models.generation.multilevel.signals.MultiLevelModelStandardSignals;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.StartGeneration;

public class StartMultiLevelGeneration extends StartGeneration {
    public StartMultiLevelGeneration() {
        super(StartMultiLevelGeneration.class);
    }

    @Override
    public String getSuccessSignalId() {
        if (getPayload().getData().get(StandardModelData.CanvasModel.toString()) != null) {
            return MultiLevelModelStandardSignals.UseReverseRenderingFlow.toString();
        } else {
            return MultiLevelModelStandardSignals.UsePreprocessingFlow.toString();
        }
    }
}
