package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnHighLevelIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType.GLOBAL;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.GLOBAL_VAR_READ_PATTERN;

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
                .flatMap(input -> {
                    Set<String> globalVars = new HashSet<>();

                    // Direct GLOBAL source type
                    if (input.getSourceType() == GLOBAL) {
                        globalVars.add(input.getSource());
                    } else {
                        // Extract from scripts and expressions where source is the input value
                        String value = input.getSource();
                        if (value != null ) {
                            Matcher matcher = GLOBAL_VAR_READ_PATTERN.matcher(value);
                            while (matcher.find()) {
                                globalVars.add(matcher.group(1));
                            }
                        }
                    }
                    return globalVars.stream();
                })
                .collect(Collectors.toSet());

        final var filteredLibrary = globalVariableLibrary.getFilteredLibrary(
                var -> globalVariablesInUse.contains(var.getName()));

        getPayload().put(BpmnPromptPlaceholders.GLOBAL_VARIABLES_USED_IN_HL_MODEL.getValue(), filteredLibrary.defaultSerialize());
    }
}
