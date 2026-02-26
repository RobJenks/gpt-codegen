package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import java.util.List;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.PROCESS_CONFIG;

public class ProcessConfigNode extends ElementNode {

    protected final List<String> attributes = List.of("processId", "processName", "processDescription", "sourceSystemName", "processTypeName", "processPriority", "processDeadline", "maximumActiveProcesses");
    protected final Map<String, String> inputAliases = Map.ofEntries(
            Map.entry("processName", "workItemName"),
            Map.entry("processDescription", "workItemDescription"),
            Map.entry("sourceSystemName", "appName"),
            Map.entry("processTypeName", "workItemTypeName"),
            Map.entry("processPriority", "workItemPriority"),
            Map.entry("processDeadline", "workItemDeadline"),
            Map.entry("maximumActiveProcesses", "maximumActiveProcessInstances"));

    public ProcessConfigNode() {
        super();
    }

    public ProcessConfigNode(String id, String name) {
        super(id, name, PROCESS_CONFIG);
    }


    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        // Process configuration is not rendered as a bpmn node so this is a no-op in the flow.
        return builder.done();
    }

    public void configure(BpmnModelInstance modelInstance, String namespace) {
        Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();

        String id = this.findInput("processId").map(ElementNodeInput::getValue).orElse("not_configured");
        String name = this.findInput("processName").map(ElementNodeInput::getValue).orElse("not_configured");
        process.setId(id);
        process.setName(name);

        if (inputs != null) {
            this.inputs.forEach(input -> {
                if (attributes.contains(input.getName())) {
                    String attrName = getAttrName(input);
                    process.setAttributeValueNs(namespace, attrName, input.getValue());
                }
            });
        }
    }

    @JsonIgnore
    @Override
    protected String getAttrName(ElementNodeInput input) {
        return inputAliases.getOrDefault(input.getName(), input.getName());
    }
}
