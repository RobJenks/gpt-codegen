package org.rj.modelgen.bpmn.models.generation.multilevel.schema;

import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.Util;

public class BpmnGenerationMultiLevelSchemaDetailLevel extends ModelSchema {
    public BpmnGenerationMultiLevelSchemaDetailLevel() {
        super(Util.loadStringResource("content/models/multilevel/bpmn-multilevel-detail-level-generation-schema.json")); // TODO
    }
}
