package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.GATEWAY_EXCLUSIVE;

public class ExclusiveGatewayNode extends ElementNode {
    private static final Logger LOG = LoggerFactory.getLogger(ExclusiveGatewayNode.class);

    public ExclusiveGatewayNode() {
        super();
    }

    public ExclusiveGatewayNode(String id, String name) {
        super(id, name, GATEWAY_EXCLUSIVE);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        return builder.exclusiveGateway(id).name(name).done();
    }

    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> void renderConditionalConnections(AbstractFlowNodeBuilder<B, E> builder, ElementConnection connection, String connectionId, B outboundConnection) {
        String defaultTargetNodeId = findInput("default").map(ElementNodeInput::getValue).orElse("");
        String conditionsJson = findInput("conditions").map(ElementNodeInput::getValue).orElse("{}");

        // Set default sequence flow on the gateway node
        if (connection.getTargetNode().equals(defaultTargetNodeId)) {
            builder.getElement().setAttributeValue("default", connectionId);
        }

        // Set condition expression on the sequence flow
        Map<String, String> conditions = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            conditions = mapper.readValue(conditionsJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            LOG.warn("Failed to parse conditions JSON: {}", e.getMessage());
        }

        outboundConnection.condition(connection.getDescription(), conditions.getOrDefault(connection.getTargetNode(), ""));
    }
}
