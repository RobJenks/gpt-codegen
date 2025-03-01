package org.rj.modelgen.bpmn.models.generation.base.context;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;

import java.util.List;

public class BpmnGenerationPromptGenerator extends TemplatedPromptGenerator<BpmnGenerationPromptGenerator> {
    public static BpmnGenerationPromptGenerator create(String generationPrompt,
                                                       String schemaErrorCorrectionPrompt,
                                                       String bpmnErrorCorrectionPrompt) {

        // BPMN generation will generate an initial model response containing simple Start -> End BPMN model,
        // if this is the first user prompt.  Allows all user prompts to be identical regardless of current state
        final var generationInitialPrompt = buildInitialNodeData().serialize();

        return new BpmnGenerationPromptGenerator()
                .withAvailablePrompt(BpmnGenerationPromptType.Generate, generationPrompt)
                .withAvailablePrompt(BpmnGenerationPromptType.GenerationInitialPrompt, generationInitialPrompt)
                .withAvailablePrompt(BpmnGenerationPromptType.CorrectSchemaErrors, schemaErrorCorrectionPrompt)
                .withAvailablePrompt(BpmnGenerationPromptType.CorrectBpmnErrors, bpmnErrorCorrectionPrompt);
    }

    private BpmnGenerationPromptGenerator() {
        super();
    }


    private static BpmnIntermediateModel buildInitialNodeData() {
        final var nodeData = new BpmnIntermediateModel();

        final var startNode = new ElementNode("startProcess", "Start Process", "startEvent");
        startNode.setConnectedTo(List.of(new ElementConnection("endProcess", "End the process immediately")));
        nodeData.getNodes().add(startNode);
        nodeData.getNodes().add(new ElementNode("endProcess", "End Process", "endEvent"));

        return nodeData;
    }
}
