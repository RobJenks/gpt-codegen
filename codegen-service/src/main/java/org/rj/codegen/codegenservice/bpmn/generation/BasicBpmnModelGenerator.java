package org.rj.codegen.codegenservice.bpmn.generation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.*;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.rj.codegen.codegenservice.bpmn.beans.ConnectionNode;
import org.rj.codegen.codegenservice.bpmn.beans.ElementNode;
import org.rj.codegen.codegenservice.bpmn.beans.NodeData;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.rj.codegen.codegenservice.bpmn.generation.BpmnConstants.*;

public class BasicBpmnModelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BasicBpmnModelGenerator.class);

    public BasicBpmnModelGenerator() { }

    public BpmnModelInstance generateModel(NodeData nodeData) {
        if (nodeData == null) throw new RuntimeException("Cannot generate model with null node data");

        LOG.info("Generating BPMN model data for graph: {}", Util.serializeOrThrow(nodeData));

        final var elementsByName = nodeData.getElements().stream()
                .collect(Collectors.toMap(ElementNode::getId, Function.identity()));

        // Map of node -> connected nodes
        final Map<String, List<String>> connected = new HashMap<>();
        for (final var conn : nodeData.getConnections()) {
            connected.computeIfAbsent(conn.getSource(), __ -> new ArrayList<>())
                    .add(conn.getDest());
        }

        final var start = nodeData.getElements().stream()
                .filter(node -> NodeTypes.START_EVENT.equals(node.getType()))
                .findFirst().orElseThrow(() -> new RuntimeException("No start event in node data"));


        // Iteratively build based on required connections; begin from a single start event
        final var nodes = new LinkedList<String>();
        final var builder = Bpmn.createExecutableProcess("process")
                .startEvent(start.getId())
                .name(start.getName())
                .done();
int tmp=0;
        nodes.add(start.getId());
        while (!nodes.isEmpty()) {
            final var node = nodes.removeFirst();

            final ModelElementInstance nodeInstance = builder.getModelElementById(node);
            if (nodeInstance == null) throw new RuntimeException("Cannot retrieve generated node: " + node);

            if (!(nodeInstance instanceof FlowNode flowNode)) continue;

            final var targets = connected.get(node);
            if (targets == null) continue;  // Nothing downstream of this node

            for (var target : targets) {
                var targetElement = elementsByName.get(target);
                if (targetElement == null) throw new RuntimeException("Target element does not exist: " + target);

//                if (NodeTypes.SEQUENCE_FLOW.equals(targetElement.getType())) {
//                    final var resolved_targets = connected.get(targetElement.getId());
//                    if (resolved_targets == null || resolved_targets.isEmpty()) throw new RuntimeException("Sequence flow has no destinations nodes");
//
//                    target = resolved_targets.get(0);
//                    targetElement = elementsByName.get(target);
//                }

                final ModelElementInstance existingTarget = builder.getModelElementById(target);
                if (existingTarget == null) {
                    addNode(flowNode.builder().sequenceFlowId("seq-" + (tmp++)), targetElement);
                    nodes.add(targetElement.getId());
                }
                else {
                    flowNode.builder().sequenceFlowId("seq-" + (tmp++)).connectTo(target);
                }
            }
        }

        return builder;
    }

    private <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode>
    void addNode(AbstractFlowNodeBuilder<B, E> builder, ElementNode element) {
        if (element == null) throw new RuntimeException("Cannot generate definition for null BPMN element");

        final var id = element.getId();
        final var name = element.getName();

        // Not differentiating between all element types for now
        final var type = element.getType();
        switch (type)
        {
            case NodeTypes.TASK_USER, NodeTypes.TASK_USER_TASK -> builder.userTask(id).name(name).done();
            case NodeTypes.TASK_SERVICE, NodeTypes.TASK_SERVICE_TASK -> builder.serviceTask(id).name(name).done();
            case NodeTypes.TASK_SCRIPT, NodeTypes.TASK_SCRIPT_TASK -> builder.scriptTask(id).name(name).done();
            case NodeTypes.TASK_MANUAL, NodeTypes.TASK_MANUAL_TASK -> builder.manualTask(id).name(name).done();
            case NodeTypes.END_EVENT -> builder.endEvent(id).name(name).done();
            case NodeTypes.GATEWAY_EXCLUSIVE -> builder.exclusiveGateway(id).name(name).done();
            //case NodeTypes.SEQUENCE_FLOW -> {}

            default -> builder.manualTask(id).name(name).done();   // TODO
        }
    }

    public BpmnModelInstance generateModelOld(NodeData nodeData) {
        if (nodeData == null) throw new RuntimeException("Cannot generate model with null node data");

        final var modelWithProcess = createModel();
        final var model = modelWithProcess.getLeft();
        final var process = modelWithProcess.getRight();

        Map<String, FlowNode> generatedElements = new HashMap<>();

        for (final var element : nodeData.getElements()) {
            final var generatedNode = addElement(element, process);
            generatedElements.put(element.getName(), generatedNode);
        }

        for (final var connection : nodeData.getConnections()) {
            final var source = generatedElements.get(connection.getSource());
            final var dest = generatedElements.get(connection.getDest());

            createSequenceFlow(process, source, dest);
        }

        return model;
    }

    public FlowNode addElement(ElementNode element, Process process) {
        if (element == null) throw new RuntimeException("Cannot generate definition for null BPMN element");

        // Not differentiating between all element types for now
        final var type = element.getType();
        final var generatedElement = switch (type)
        {
            case NodeTypes.START_EVENT -> createElement(process, element.getId(), StartEvent.class);
            case NodeTypes.END_EVENT -> createElement(process, element.getId(), EndEvent.class);
            default -> createElement(process, element.getId(), Task.class);
        };

        if (generatedElement == null) throw new RuntimeException(String.format("Failed to generate valid BPMN element for '%s'", element.getName()));

        return generatedElement;
    }

    private Pair<BpmnModelInstance, Process> createModel() {
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

        final var definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        final var process = createElement(definitions, "process", Process.class);

        return Pair.of(modelInstance, process);
    }

    protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    public SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String identifier = from.getId() + "-" + to.getId();
        SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }

    public static void main(String[] args) {
        test3();
    }

    private static void test1() {
        final var nodeData = new NodeData();

        nodeData.getElements().add(new ElementNode("start", "Start", "startEvent"));
        nodeData.getElements().add(new ElementNode("end", "End", "endEvent"));

        nodeData.getConnections().add(new ConnectionNode("start", "end", ""));

        BasicBpmnModelGenerator generator = new BasicBpmnModelGenerator();
        final var model = generator.generateModel(nodeData);



        final var serialized = Bpmn.convertToString(model);
        System.out.println(serialized);
    }

    private static void test2() {
        final var response = "{\n" +
                "    \"elements\": [\n" +
                "        {\"name\":\"start\",\"type\":\"startEvent\",\"x\":0,\"y\":100},\n" +
                "        {\"name\":\"request_approval\",\"type\":\"userTask\",\"x\":200,\"y\":75},\n" +
                "        {\"name\":\"end\",\"type\":\"endEvent\",\"x\":400,\"y\":100},\n" +
                "        {\"name\":\"service\",\"type\":\"serviceTask\",\"x\":400,\"y\":50}\n" +
                "    ],\n" +
                "    \"connections\": [\n" +
                "        {\"source\":\"start\",\"dest\":\"request_approval\",\"comment\":\"\"},\n" +
                "        {\"source\":\"request_approval\",\"dest\":\"end\",\"comment\":\"approve\"},\n" +
                "        {\"source\":\"request_approval\",\"dest\":\"service\",\"comment\":\"approve\"},\n" +
                "        {\"source\":\"request_approval\",\"dest\":\"end\",\"comment\":\"reject\"}\n" +
                "    ]\n" +
                "}";

        final var generator = new BasicBpmnModelGenerator();
        final var model = generator.generateModel(Util.deserializeOrThrow(response, NodeData.class, RuntimeException::new));

        System.out.println(Bpmn.convertToString(model));
    }

    private static void test3() {
        final var response = "{\"elements\":[{\"name\":\"start\",\"type\":\"startEvent\",\"x\":0,\"y\":100},{\"name\":\"userTask\",\"type\":\"userTask\",\"x\":200,\"y\":75},{\"name\":\"decision\",\"type\":\"exclusiveGateway\",\"x\":400,\"y\":50},{\"name\":\"approved\",\"type\":\"sequenceFlow\",\"x\":480,\"y\":150},{\"name\":\"notApproved\",\"type\":\"sequenceFlow\",\"x\":480,\"y\":50},{\"name\":\"end\",\"type\":\"endEvent\",\"x\":600,\"y\":100}],\"connections\":[{\"source\":\"start\",\"dest\":\"userTask\",\"comment\":\"\"},{\"source\":\"userTask\",\"dest\":\"decision\",\"comment\":\"\"},{\"source\":\"decision\",\"dest\":\"approved\",\"comment\":\"user approves\"},{\"source\":\"decision\",\"dest\":\"notApproved\",\"comment\":\"user does not approve\"},{\"source\":\"approved\",\"dest\":\"end\",\"comment\":\"\"},{\"source\":\"notApproved\",\"dest\":\"end\",\"comment\":\"\"}]}";
        final var generator = new BasicBpmnModelGenerator();
        final var model = generator.generateModel(Util.deserializeOrThrow(response, NodeData.class, RuntimeException::new));

        System.out.println(Bpmn.convertToString(model));
    }

}
