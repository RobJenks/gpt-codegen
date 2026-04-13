package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.BusinessRuleTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.CommonTaskConstants.OUTPUT_VAR_ATTR;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.CommonTaskConstants.RESPONSE_VAR;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_BUSINESS_RULE_TASK;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.BusinessRuleTaskConstants.*;

public class BusinessRuleTaskNode extends ElementNode {

    public BusinessRuleTaskNode() {
        super();
    }

    public BusinessRuleTaskNode(String id, String name) {
        super(id, name, TASK_BUSINESS_RULE_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, BpmnComponent elementDefinition, String namespace) {
        BusinessRuleTaskBuilder taskBuilder = builder.businessRuleTask(id).name(name);
        BusinessRuleTask task = taskBuilder.getElement();

        // Set default value for output variable
        task.setAttributeValueNs(namespace, OUTPUT_VAR_ATTR, RESPONSE_VAR);

        if (inputs != null) {
            inputs.forEach(input -> {
                String attrName = getAttrName(input, elementDefinition);
                if (ATTRIBUTES.contains(input.getName())) {
                    task.setAttributeValueNs(namespace, attrName, input.getValue());
                } else if (EXTENSIONS.contains(input.getName())) {
                    ExtensionElements extensionElements = getExtensionElements(task);
                    ModelElementInstance extensionElement = extensionElements.addExtensionElement(namespace, attrName);
                    extensionElement.setTextContent(input.getValue());
                }
            });
        }

        return taskBuilder.done();
    }
}
