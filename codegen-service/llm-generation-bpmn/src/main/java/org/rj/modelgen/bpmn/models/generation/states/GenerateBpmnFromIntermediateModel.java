package org.rj.modelgen.bpmn.models.generation.states;

import org.rj.modelgen.bpmn.generation.BasicBpmnModelGenerator;
import org.rj.modelgen.bpmn.intrep.bpmn.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.signals.BpmnXmlSuccessfullyGeneratedFromModelResponse;
import org.rj.modelgen.bpmn.models.generation.signals.LlmResponseModelDataIsValid;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import reactor.core.publisher.Mono;

import java.util.List;

public class GenerateBpmnFromIntermediateModel extends ModelInterfaceState {
    private final IntermediateModelParser<BpmnIntermediateModel> modelParser;
    private final BasicBpmnModelGenerator bpmnGenerator;

    public GenerateBpmnFromIntermediateModel() {
        super(GenerateBpmnFromIntermediateModel.class);
        this.modelParser = new IntermediateModelParser<>(BpmnIntermediateModel.class);
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

        return outboundSignal(new BpmnXmlSuccessfullyGeneratedFromModelResponse(
                input.getSanitizedResponseContent(), generatedBpmn.getValue(), List.of()));
    }
}
