package org.rj.modelgen.bpmn.models.generation.context;

import org.rj.modelgen.bpmn.beans.ElementNode;
import org.rj.modelgen.bpmn.beans.NodeData;
import org.rj.modelgen.llm.prompt.PromptGenerator;

import java.util.List;

public class BpmnGenerationPromptGenerator extends PromptGenerator<BpmnGenerationPromptGenerator, BpmnGenerationPromptType> {
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


    private static NodeData buildInitialNodeData() {
        final var nodeData = new NodeData();

        final var startNode = new ElementNode("startProcess", "Start Process", "startEvent");
        startNode.setConnectedTo(List.of(new ElementNode.Connection("endProcess", "End the process immediately")));
        nodeData.getNodes().add(startNode);
        nodeData.getNodes().add(new ElementNode("endProcess", "End Process", "endEvent"));

        return nodeData;
    }
}
