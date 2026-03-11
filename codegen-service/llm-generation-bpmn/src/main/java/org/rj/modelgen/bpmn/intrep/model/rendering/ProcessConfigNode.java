package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.PROCESS_CONFIG;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.ProcessConfigConstants.*;

public class ProcessConfigNode extends ElementNode {

    public ProcessConfigNode() {
        super();
    }

    public ProcessConfigNode(String id, String name) {
        super(id, name, PROCESS_CONFIG);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, BpmnComponent elementDefinition, String namespace) {
        // Process configuration is not rendered as a bpmn node so this is a no-op in the flow.
        return builder.done();
    }

    public void configure(BpmnModelInstance modelInstance, BpmnComponent elementDefinition, String namespace) {
        Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();

        String id = this.findInput(PROCESS_ID).map(ElementNodeInput::getValue).orElse(ATTR_NOT_CONFIGURED);
        String name = this.findInput(PROCESS_NAME).map(ElementNodeInput::getValue).orElse(ATTR_NOT_CONFIGURED);
        process.setId(id);
        process.setName(name);
        process.setAttributeValueNs(namespace, PROCESS_NAME_ATTR, name);

        if (inputs != null) {
            this.inputs.forEach(input -> {
                if (ATTRIBUTES.contains(input.getName())) {
                    String attrName = getAttrName(input, elementDefinition);
                    process.setAttributeValueNs(namespace, attrName, input.getValue());
                }
            });
        }
    }
}
