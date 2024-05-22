package org.rj.modelgen.bpmn.models.generation.multilevel.schema;

import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.Util;

public class BpmnGenerationMultiLevelSchemaHighLevel extends ModelSchema {
    public BpmnGenerationMultiLevelSchemaHighLevel() {
        super(Util.loadStringResource("content/models/multilevel/bpmn-multilevel-high-level-generation-schema.json"));
    }
}
