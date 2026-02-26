package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import java.util.List;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_SERVICE_TASK;

public class ServiceTaskNode extends ElementNode {

    protected final List<String> attributes = List.of("serviceUrl", "uriPath", "httpMethod", "inputExpression", "outputVariable");
    protected final List<String> extensions = List.of("inputScript", "outputScript");
    protected final Map<String, String> inputAliases = Map.ofEntries(
            Map.entry("serviceUrl", "httpRootUrl"),
            Map.entry("uriPath", "httpPath"),
            Map.entry("httpMethod", "httpServiceMethod"));

    public ServiceTaskNode() {
        super();
    }

    public ServiceTaskNode(String id, String name) {
        super(id, name, TASK_SERVICE_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        ServiceTaskBuilder taskBuilder = builder.serviceTask(id).name(name);
        ServiceTask task = taskBuilder.getElement();

        // Set default value for output variable
        task.setAttributeValueNs(namespace, "outputVariable", "response");

        if (inputs != null) {
            inputs.forEach(input -> {
                if (attributes.contains(input.getName())) {
                    String attrName = getAttrName(input);
                    task.setAttributeValueNs(namespace, attrName, input.getValue());
                } else if (extensions.contains(input.getName())) {
                    ExtensionElements extensionElements = getExtensionElements(task);
                    ModelElementInstance extensionElement = extensionElements.addExtensionElement(namespace, input.getName());
                    extensionElement.setTextContent(input.getValue());
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
