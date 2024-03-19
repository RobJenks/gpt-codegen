package org.rj.modelgen.bpmn.intrep.schema;

import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.util.Util;

public class BpmnIntermediateModelSchema extends ModelSchema {
    public BpmnIntermediateModelSchema() {
        super(Util.loadStringResource("content/bpmn-intermediate-schema.json"));
    }
}
