package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariable;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.models.generation.multilevel.options.BpmnMultiLevelGenerationModelOptions;
import org.rj.modelgen.bpmn.models.generation.validation.PayloadVariable;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders.*;

public class InitializeBpmnData extends ExecuteLogic {
    private final BpmnComponentLibrary componentLibrary;
    private final BpmnGlobalVariableLibrary globalVariableLibrary;
    private final BpmnMultiLevelGenerationModelOptions options;

    public InitializeBpmnData(BpmnComponentLibrary componentLibrary, BpmnGlobalVariableLibrary globalVariableLibrary, BpmnMultiLevelGenerationModelOptions options) {
        super(InitializeBpmnData.class);
        this.componentLibrary = componentLibrary;
        this.globalVariableLibrary = globalVariableLibrary;
        this.options = options;
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        // Insert additional BPMN data into the model payload
        getPayload().put(GLOBAL_VARIABLE_LIBRARY.getValue(), globalVariableLibrary.defaultSerialize());

        if (options.shouldAddPromptSpecificGlobalVariables()) {
            // Insert filtered set of variables detected in the initial prompt, for use in preprocessing phases
            final var prompt = getPayload().getOrElse(MultiLevelModelStandardPayloadData.Request, "");
            final var promptRelevantGlobalVariables = globalVariableLibrary.getFilteredBasedOnPrompt(prompt);
            getPayload().put(PROMPT_RELEVANT_GLOBAL_VARIABLES.getValue(), promptRelevantGlobalVariables.defaultSerialize());
        }

        // Enable use of placeholders for unknown action types if required
        if (options.shouldAddPlaceholderForUnknownComponents()) {
            getPayload().put(MultiLevelModelStandardPayloadData.AddPlaceholdersForUnknownActions, true);
        }

        if (options.shouldAddStartingPayloadVariables()) {
            final String processVariables = getPayload().get(MultiLevelModelStandardPayloadData.ProcessVariables);
            final var startingPayloadVariables = initializeStartingPayload(processVariables, componentLibrary);
            // Store the starting payload variables for use in later phases
            getPayload().put(MultiLevelModelStandardPayloadData.ProcessVariables, startingPayloadVariables);

            // Also serialize them for use in prompt generation
            final String serializedStartingPayload =  startingPayloadVariables.stream()
                    .map(v -> String.format("- %s (%s)", v.getName(), v.getType()))
                    .collect(Collectors.joining("\n"));
            getPayload().put(STARTING_PAYLOAD_VARIABLES.getValue(), serializedStartingPayload);
        }

        return Mono.just(Result.Ok());
    }

    private Set<PayloadVariable> initializeStartingPayload(String processVariables, BpmnComponentLibrary componentLibrary) {
        List<PayloadVariable> processVariablesList;
        try {
            ObjectMapper mapper = new ObjectMapper();
            processVariablesList = mapper.readValue(processVariables, new TypeReference<>() {});

        } catch (Exception e) {
            processVariablesList = Collections.emptyList();
        }

        List<PayloadVariable> automaticallyGeneratedOutputs = componentLibrary.getComponents().stream()
                .flatMap(component -> component.getGeneratedOutputs().stream())
                .map(variable -> new PayloadVariable(variable.getName(), variable.getType().toString()))
                .toList();

        Set<String> globalVarResolveValues = globalVariableLibrary.getComponents().stream()
                .map(BpmnGlobalVariable::getResolveValue)
                .collect(Collectors.toSet());

        // Compute starting payload by removing automatically generated outputs and global vars from the read variables
        return processVariablesList.stream()
                .filter(x -> !automaticallyGeneratedOutputs.contains(x) && !globalVarResolveValues.contains(x.getName()))
                .collect(Collectors.toSet());
    }
}
