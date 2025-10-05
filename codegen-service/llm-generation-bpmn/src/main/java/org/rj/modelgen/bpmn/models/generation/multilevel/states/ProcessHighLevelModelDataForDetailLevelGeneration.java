package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnHighLevelIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementHighLevelNodeInput;
import org.rj.modelgen.bpmn.intrep.model.ElementHighLevelNodeInputSourceType;
import org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public class ProcessHighLevelModelDataForDetailLevelGeneration extends ExecuteLogic {
    private BpmnGlobalVariableLibrary globalVariableLibrary;

    public ProcessHighLevelModelDataForDetailLevelGeneration(BpmnGlobalVariableLibrary globalVariableLibrary) {
        super(ProcessHighLevelModelDataForDetailLevelGeneration.class);
        this.globalVariableLibrary = globalVariableLibrary;
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        final String serializedModel = getPayload().get(MultiLevelModelStandardPayloadData.HighLevelModel);
        final var model = Util.deserializeOrThrow(serializedModel, BpmnHighLevelIntermediateModel.class);
        if (model == null) {
            return Mono.just(Result.Err("Cannot process high-level model data; no valid model was generated"));
        }

        // Analyze high-level model data and add derived data to the payload ready for detail-level generation
        determineGlobalVariablesInUse(model);

        return Mono.just(Result.Ok());
    }

    private void determineGlobalVariablesInUse(BpmnHighLevelIntermediateModel model) {
        final var globalVariablesInUse = model.getNodes().stream()
                .flatMap(node -> node.getInputs().stream())
                .filter(input -> input.getSourceType() == ElementHighLevelNodeInputSourceType.Global)
                .map(ElementHighLevelNodeInput::getSource)
                .collect(Collectors.toSet());

        final var filteredLibrary = globalVariableLibrary.getFilteredLibrary(
                var -> globalVariablesInUse.contains(var.getName()));

        getPayload().put(BpmnPromptPlaceholders.GLOBAL_VARIABLES_USED_IN_HL_MODEL.getValue(), filteredLibrary.defaultSerialize());
    }
}
