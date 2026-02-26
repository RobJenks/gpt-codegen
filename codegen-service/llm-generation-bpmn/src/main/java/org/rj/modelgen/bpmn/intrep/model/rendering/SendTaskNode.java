package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.SendTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import java.util.List;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_SEND_TASK;

public class SendTaskNode extends ElementNode {

    protected final List<String> attributes = List.of("serviceUrl", "uriPath", "httpMethod", "inputExpression", "outputVariable");
    protected final List<String> extensions = List.of("inputScript", "outputScript");
    protected final Map<String, String> inputAliases = Map.ofEntries(
            Map.entry("serviceUrl", "httpRootUrl"),
            Map.entry("uriPath", "httpPath"),
            Map.entry("httpMethod", "httpServiceMethod"));

    public SendTaskNode() {
        super();
    }

    public SendTaskNode(String id, String name) {
        super(id, name, TASK_SEND_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        SendTaskBuilder taskBuilder = builder.sendTask(id).name(name);
        SendTask task = taskBuilder.getElement();

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
