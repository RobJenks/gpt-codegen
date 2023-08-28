package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlDataPassedValidation;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlSuccessfullyGeneratedFromModelResponse;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;

public class ValidateBpmnModelCorrectness extends ModelInterfaceState<BpmnXmlSuccessfullyGeneratedFromModelResponse> {
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

        return Mono.just(new BpmnXmlDataPassedValidation(
                "VALID-" + input.getGeneratedBpmn(),
                input.getModelValidationMessages(),
                List.of("X", "Y", "Z"))
        );
    }
}
