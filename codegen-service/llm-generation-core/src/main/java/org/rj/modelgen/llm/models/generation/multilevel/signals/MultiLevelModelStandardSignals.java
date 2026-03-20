package org.rj.modelgen.llm.models.generation.multilevel.signals;

import org.rj.modelgen.llm.util.StringSerializable;

public enum MultiLevelModelStandardSignals implements StringSerializable {
    StartGeneration,
    ReturnToHighLevel,
    RetryDetailLevel,
    UsePreprocessingFlow,
    UseReverseRenderingFlow;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
