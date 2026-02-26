package org.rj.modelgen.bpmn.intrep.model;

import org.rj.modelgen.bpmn.intrep.model.rendering.*;

import java.util.HashMap;
import java.util.Map;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.*;

public class ElementNodeTypeRegistry {

    private static final Map<String, Class<? extends ElementNode>> typeMap = new HashMap<>();

    static {
        registerType(TASK_USER_TASK, UserTaskNode.class);
        registerType(TASK_SERVICE_TASK, ServiceTaskNode.class);
        registerType(TASK_SCRIPT_TASK, ScriptTaskNode.class);
        registerType(TASK_RECEIVE_TASK, ReceiveTaskNode.class);
        registerType(TASK_SEND_TASK, SendTaskNode.class);
        registerType(TASK_BUSINESS_RULE_TASK, BusinessRuleTaskNode.class);
        registerType(GATEWAY_EXCLUSIVE, ExclusiveGatewayNode.class);
        registerType(PROCESS_CONFIG, ProcessConfigNode.class);
    }

    public static void registerType(String elementType, Class<? extends ElementNode> nodeClass) {
        typeMap.put(elementType, nodeClass);
    }

    public static Class<? extends ElementNode> getNodeClass(String elementType) {
        return typeMap.getOrDefault(elementType, ElementNode.class);
    }
}
