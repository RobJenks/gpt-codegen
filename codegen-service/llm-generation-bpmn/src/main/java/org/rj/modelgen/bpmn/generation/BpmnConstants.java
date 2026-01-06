package org.rj.modelgen.bpmn.generation;

import reactor.netty.udp.UdpServer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BpmnConstants {
    public static class NodeTypes {
        public static class Comparable {
            public static final String TASK = "task";
            public static final String USER_TASK = "usertask";
            public static final String SERVICE_TASK = "servicetask";
            public static final String SCRIPT_TASK = "scripttask";
            public static final String BUSINESS_RULE_TASK = "businessruletask";
        }

        public static final String TASK = "task";
        public static final String TASK_USER = "user";
        public static final String TASK_USER_TASK = "userTask";
        public static final String TASK_SERVICE = "service";
        public static final String TASK_SERVICE_TASK = "serviceTask";
        public static final String TASK_SCRIPT = "script";
        public static final String TASK_SCRIPT_TASK = "scriptTask";
        public static final String TASK_BUSINESS_RULE = "businessRule";
        public static final String TASK_BUSINESS_RULE_TASK = "businessRuleTask";
        public static final String TASK_MANUAL = "manual";
        public static final String TASK_MANUAL_TASK = "manualTask";
        public static final String TASK_RECEIVE = "receive";
        public static final String TASK_RECEIVE_TASK = "receiveTask";
        public static final String TASK_SEND = "send";
        public static final String TASK_SEND_TASK = "sendTask";
        public static final String TASK_CALL = "call";
        public static final String TASK_CALL_TASK = "callTask";

        public static final String GATEWAY_EXCLUSIVE = "exclusiveGateway";
        public static final String GATEWAY_INCLUSIVE = "inclusiveGateway";
        public static final String GATEWAY_PARALLEL = "parallelGateway";
        public static final String GATEWAY_SUFFIX = "Gateway";

        public static final String START_EVENT = "startEvent";
        public static final String END_EVENT = "endEvent";

        public static final String SEQUENCE_FLOW = "sequenceFlow";
    }

    public static class Patterns {
        // Matches getVariable('varName', 'sourceNodeId') or ${getVariable("varName", "sourceNodeId")}
        public static final Pattern VAR_READ_PATTERN = Pattern.compile("(?:\\$\\{)?getVariable\\s*\\(\\s*['\"]([a-zA-Z_][a-zA-Z0-9_.]*)['\"](?:\\s*,\\s*['\"]([a-zA-Z_][a-zA-Z0-9_]*)['\"])?\\s*\\)(?:\\})?");
        // Matches setVariable('varName', value, 'varType')
        public static final Pattern VAR_WRITE_PATTERN = Pattern.compile("setVariable\\s*\\(\\s*['\"]?([a-zA-Z_][a-zA-Z0-9_.]*)['\"]?\\s*,\\s*([^,]+?)\\s*,\\s*['\"]([a-zA-Z_][a-zA-Z0-9_]*)['\"]\\s*\\)");
        // Matches throw('errorMessage')
        public static final Pattern THROW_ERROR_PATTERN = Pattern.compile("throw\\s*\\(\\s*(.+?)\\s*\\)");
        // Matches getGlobalVariable('varName', [arg1, arg2])
        public static final Pattern GLOBAL_VAR_READ_PATTERN = Pattern.compile("getGlobalVariable\\s*\\(\\s*['\"]([^'\"]+)['\"](?:\\s*,\\s*\\[([^\\]]*)\\])?\\s*\\)");
        // Matches ${variableName} but not ${payload.variableName}
        public static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{(?!payload\\.)([^}]+)\\}");
    }

}
