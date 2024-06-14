package org.rj.modelgen.bpmn.component;

import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementNode;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.state.ModelInterfacePayload;
import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;

import java.util.stream.Collectors;

public class BpmnComponentLibraryDetailLevelSelector implements ComponentLibrarySelector<BpmnComponentLibrary> {
    @Override
    public BpmnComponentLibrary getFilteredLibrary(BpmnComponentLibrary baseLibrary, ModelInterfacePayload payload) {
        final var model = getLatestIntermediateModel(payload);

        // Filter down to only the types in use in the high level model
        final var typesInUse = model.getNodes().stream()
                .map(ElementNode::getElementType)
                .collect(Collectors.toSet());

        return new BpmnComponentLibrary(baseLibrary.getFilteredLibrary(comp -> typesInUse.contains(comp.getName())).getComponents());
    }

    private BpmnIntermediateModel getLatestIntermediateModel(ModelInterfacePayload payload) {
        final String content = payload.get(StandardModelData.SanitizedContent);
        final var parser = new IntermediateModelParser<>(BpmnIntermediateModel.class);

        return parser.parse(content).orElseThrow(e -> new LlmGenerationModelException(String.format(
                "Component library selector could not parse intermediate model: %s (content: %s)", e, content)));
    }
}
