package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.generation.BasicBpmnModelGenerator;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlSuccessfullyGeneratedFromModelResponse;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.llm.schema.IntermediateModelParser;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;

public class GenerateBpmnFromIntermediateModel extends ModelInterfaceState<LlmResponseModelDataIsValid> {
    private final IntermediateModelParser modelParser;
    private final BasicBpmnModelGenerator bpmnGenerator;

    public GenerateBpmnFromIntermediateModel() {
        super(GenerateBpmnFromIntermediateModel.class);
        this.modelParser = new IntermediateModelParser();
        this.bpmnGenerator = new BasicBpmnModelGenerator();
    }

    @Override
    public String getDescription() {
        return "Generate BPMN XML from intermediate representation returned by LLM";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        final var input = asExpectedInputSignal(inputSignal);

        final var intermediateModel = modelParser.parse(input.getSanitizedResponseContent());
        if (intermediateModel.isErr()) {
            // TODO: Generate error signal
        }

        final var generatedBpmn = bpmnGenerator.generateModel(intermediateModel.getValue());
        if (generatedBpmn.isErr()) {
            // TODO: Generate error signal
        }

        return Mono.just(new BpmnXmlSuccessfullyGeneratedFromModelResponse(
                input.getSanitizedResponseContent(), generatedBpmn.getValue(), List.of()));
    }
}
