package org.rj.modelgen.bpmn.models.generation.multilevel.prompt;

import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelGenerationModelPromptGenerator;
import org.rj.modelgen.llm.models.generation.multilevel.prompt.MultiLevelModelPromptType;
import org.rj.modelgen.llm.util.Util;

public class BpmnGenerationMultiLevelPromptGenerator extends MultiLevelGenerationModelPromptGenerator {
    public BpmnGenerationMultiLevelPromptGenerator() {
        addPrompt(MultiLevelModelPromptType.GenerateHighLevel, Util.loadStringResource("content/models/multilevel/bpmn-multilevel-high-level-generation-prompt"));
        addPrompt(MultiLevelModelPromptType.GenerateDetailLevel, Util.loadStringResource("content/models/multilevel/bpmn-multilevel-detail-level-generation-prompt"));
    }
}
