package org.rj.modelgen.bpmn.models.generation.base.states;

import org.rj.modelgen.bpmn.generation.BasicBpmnModelGenerator;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.base.signals.BpmnGenerationSignals;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;
import reactor.core.publisher.Mono;


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
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal input) {

        final String sanitizedContent = getPayload().get(StandardModelData.SanitizedContent);
        final var intermediateModel = modelParser.parse(sanitizedContent);
        if (intermediateModel.isErr()) {
            // TODO: Generate error signal
        }

        final var generatedBpmn = bpmnGenerator.generateModel(intermediateModel.getValue());
        if (generatedBpmn.isErr()) {
            // TODO: Generate error signal
        }

        return outboundSignal(BpmnGenerationSignals.ValidateBpmnXml)
                .withPayloadData(StandardModelData.IntermediateModel, intermediateModel.getValue())
                .withPayloadData(StandardModelData.GeneratedModel, generatedBpmn.getValue())
                .mono();
    }
}
