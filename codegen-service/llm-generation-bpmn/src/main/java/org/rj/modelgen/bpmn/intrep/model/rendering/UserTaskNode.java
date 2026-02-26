package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import java.util.List;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_USER_TASK;

public class UserTaskNode extends ElementNode {

    protected final List<String> attributes = List.of("taskName", "taskDescription", "sourceSystemName", "taskPriority", "taskDeadline", "maximumActiveTasks");
    protected final Map<String, String> inputAliases = Map.ofEntries(
            Map.entry("taskName", "workItemName"),
            Map.entry("taskDescription", "workItemDescription"),
            Map.entry("sourceSystemName", "appName"),
            Map.entry("taskTypeName", "workItemTypeName"),
            Map.entry("taskPriority", "workItemPriority"),
            Map.entry("taskDeadline", "workItemDeadline"));

    public UserTaskNode() {
        super();
    }

    public UserTaskNode(String id, String name) {
        super(id, name, TASK_USER_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        UserTaskBuilder taskBuilder = builder.userTask(id).name(name);
        UserTask task = taskBuilder.getElement();

        if (inputs != null) {
            inputs.forEach(input -> {
                if (attributes.contains(input.getName())) {
                    String attrName = getAttrName(input);
                    task.setAttributeValueNs(namespace, attrName, input.getValue());
                }
            });
        }

        return taskBuilder.done();
    }

    @JsonIgnore
    @Override
    protected String getAttrName(ElementNodeInput input) {
        return inputAliases.getOrDefault(input.getName(), input.getName());
    }
}
