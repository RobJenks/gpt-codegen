package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.rj.modelgen.bpmn.intrep.model.ElementConnection;

import java.util.Map;

public interface ConditionalGateway {
    @JsonIgnore
    String getDefaultTargetNodeId();

    @JsonIgnore
    Map<String, String> getConditions();

    @JsonIgnore
    <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> void renderConditionalConnections(
            AbstractFlowNodeBuilder<B, E> builder,
            ElementConnection connection,
            String connectionId,
            B outboundConnection
    );
}
