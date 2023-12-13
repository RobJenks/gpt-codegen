package org.rj.modelgen.bpmn.models.generation.states;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlDataPassedValidation;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlSuccessfullyGeneratedFromModelResponse;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class ValidateBpmnModelCorrectness extends ModelInterfaceState<BpmnXmlSuccessfullyGeneratedFromModelResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(ValidateBpmnModelCorrectness.class);

    public ValidateBpmnModelCorrectness() {
        super(ValidateBpmnModelCorrectness.class);
    }

    @Override
    public String getDescription() {
        return "Validate correctness of generated BPMN model";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        final var bpmn = input.getGeneratedBpmn();
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
        }

        // Additional custom validations

        // All validations successful
        return outboundSignal(new BpmnXmlDataPassedValidation(input.getIntermediateModel(), bpmn, List.of()));
    }
}
