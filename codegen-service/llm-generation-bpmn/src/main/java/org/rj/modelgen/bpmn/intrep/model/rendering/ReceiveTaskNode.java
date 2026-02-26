package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ReceiveTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_RECEIVE_TASK;

public class ReceiveTaskNode extends ElementNode {

    public ReceiveTaskNode() {
        super();
    }

    public ReceiveTaskNode(String id, String name) {
        super(id, name, TASK_RECEIVE_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        ReceiveTaskBuilder taskBuilder = builder.receiveTask(id).name(name);
        ReceiveTask task = taskBuilder.getElement();
        BpmnModelInstance modelInstance = (BpmnModelInstance) task.getModelInstance();

        String messageId = findInput("messageId")
                .map(ElementNodeInput::getValue)
                .orElse(null);

        if (messageId != null) {
            Message message = modelInstance.getModelElementById(messageId);
            if (message == null) {
                Definitions definitions = modelInstance.getDefinitions();
                message = modelInstance.newInstance(Message.class);
                String randomId = java.util.UUID.randomUUID().toString().substring(0, 7);
                message.setId("Message_" + randomId);
                message.setName(messageId);

                definitions.addChildElement(message);
            }
            task.setMessage(message);
        }

        // Set availability script
        String availability = findInput("availabilityRuleScript")
                .map(ElementNodeInput::getValue)
                .orElse("// not configured");

        task.setAttributeValueNs(namespace, "availability", availability);

        return taskBuilder.done();
    }
}
