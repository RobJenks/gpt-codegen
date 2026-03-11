package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.CommonTaskConstants.*;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_SERVICE_TASK;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.ServiceTaskConstants.*;

public class ServiceTaskNode extends ElementNode {

    public ServiceTaskNode() {
        super();
    }

    public ServiceTaskNode(String id, String name) {
        super(id, name, TASK_SERVICE_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, BpmnComponent elementDefinition, String namespace) {
        ServiceTaskBuilder taskBuilder = builder.serviceTask(id).name(name);
        ServiceTask task = taskBuilder.getElement();

        // Set default value for output variable
        task.setAttributeValueNs(namespace, OUTPUT_VAR_ATTR, RESPONSE_VAR);

        if (inputs != null) {
            inputs.forEach(input -> {
                if (ATTRIBUTES.contains(input.getName())) {
                    String attrName = getAttrName(input, elementDefinition);
                    task.setAttributeValueNs(namespace, attrName, input.getValue());
                } else if (EXTENSIONS.contains(input.getName())) {
                    ExtensionElements extensionElements = getExtensionElements(task);
                    ModelElementInstance extensionElement = extensionElements.addExtensionElement(namespace, input.getName());
                    extensionElement.setTextContent(input.getValue());
                }
            });
        }

        return taskBuilder.done();
    }
}
