package org.rj.modelgen.bpmn.models.generation.common.states;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.rj.modelgen.bpmn.models.generation.base.signals.*;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ValidateBpmnModelCorrectness extends ModelInterfaceState {
    private static final Logger LOG = LoggerFactory.getLogger(ValidateBpmnModelCorrectness.class);

    public ValidateBpmnModelCorrectness() {
        super(ValidateBpmnModelCorrectness.class);
    }

    @Override
    public String getDescription() {
        return "Validate correctness of generated BPMN model";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {

        for (Map.Entry<String, Object> entry : getPayload().getData().entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        //var bpmnContent = getPayload().getData().get(StandardModelData.ResponseContent);
        final BpmnModelInstance bpmn = getPayload().get(StandardModelData.ResponseContent);
        if (bpmn == null) {
            // TODO: Generate error signal
        }

        // Delegate to Camunda model validation
        try {
            Bpmn.validateModel(bpmn);
        }
        catch (ModelValidationException ex) {
            // TODO: Generate error signal
            LOG.error("BPMN model failed validation: {}", ex.getMessage());
            getPayload().put(StandardModelData.ModelValidationMessages, ex.getMessage());
        }

        // Additional custom validations

        // All validations successful
        return outboundSignal(BpmnGenerationSignals.CompleteGeneration)
                .withPayloadData(StandardModelData.ModelValidationMessages, List.of())  // TODO: Append validation messages
                .mono();
    }
}
