package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.BusinessRuleTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import java.util.List;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_BUSINESS_RULE_TASK;

public class BusinessRuleTaskNode extends ElementNode {

    protected final List<String> attributes = List.of("executionEngine", "applicationId", "ruleName", "ruleVersion");
    protected final Map<String, String> inputAliases = Map.ofEntries(
            Map.entry("ruleName", "businessRuleName"),
            Map.entry("ruleVersion", "businessRuleVersion"));

    public BusinessRuleTaskNode() {
        super();
    }

    public BusinessRuleTaskNode(String id, String name) {
        super(id, name, TASK_BUSINESS_RULE_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        BusinessRuleTaskBuilder taskBuilder = builder.businessRuleTask(id).name(name);
        BusinessRuleTask task = taskBuilder.getElement();

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
