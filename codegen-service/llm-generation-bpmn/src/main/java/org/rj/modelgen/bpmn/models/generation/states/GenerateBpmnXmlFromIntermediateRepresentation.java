package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlSuccessfullyGeneratedFromModelResponse;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.bpmn.models.generation.signals.NewBpmnGenerationRequestReceived;
import org.rj.modelgen.bpmn.models.generation.signals.LlmModelRequestPreparedSuccessfully;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

public class GenerateBpmnXmlFromIntermediateRepresentation extends ModelInterfaceState<LlmResponseModelDataIsValid> {
    public GenerateBpmnXmlFromIntermediateRepresentation() {
        super(GenerateBpmnXmlFromIntermediateRepresentation.class);
    }

    @Override
    public String getDescription() {
        return "Generate BPMN XML from intermediate representation returned by LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        return Mono.just(new BpmnXmlSuccessfullyGeneratedFromModelResponse("BPMN { " + input.getModelResponse() + " }", input.getValidationMessages()));
    }
}
