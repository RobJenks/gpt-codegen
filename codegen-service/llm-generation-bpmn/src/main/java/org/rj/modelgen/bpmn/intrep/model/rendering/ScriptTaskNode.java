package org.rj.modelgen.bpmn.intrep.model.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ScriptTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Script;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.NodeTypes.TASK_SCRIPT_TASK;

public class ScriptTaskNode extends ElementNode {
    public ScriptTaskNode() {
        super();
    }

    public ScriptTaskNode(String id, String name) {
        super(id, name, TASK_SCRIPT_TASK);
    }

    @JsonIgnore
    @Override
    public <B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> BpmnModelInstance render(AbstractFlowNodeBuilder<B, E> builder, String namespace) {
        ScriptTaskBuilder taskBuilder = builder.scriptTask(id).name(name);
        ScriptTask task = taskBuilder.getElement();

        String scriptContent = findInput("script")
                .map(ElementNodeInput::getValue)
                .orElse("");

        task.setScriptFormat("groovy");
        BpmnModelInstance modelInstance = (BpmnModelInstance) task.getModelInstance();
        Script scriptElement = modelInstance.newInstance(Script.class);
        scriptElement.setTextContent(scriptContent);
        task.setScript(scriptElement);

        return taskBuilder.done();
    }
}
