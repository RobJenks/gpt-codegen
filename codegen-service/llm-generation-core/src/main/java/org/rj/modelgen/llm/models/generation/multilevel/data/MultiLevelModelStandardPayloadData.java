package org.rj.modelgen.llm.models.generation.multilevel.data;

public enum MultiLevelModelStandardPayloadData {
    HighLevelModel,
    DetailLevelModel;

    @Override
    public String toString() {
        return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }
}
