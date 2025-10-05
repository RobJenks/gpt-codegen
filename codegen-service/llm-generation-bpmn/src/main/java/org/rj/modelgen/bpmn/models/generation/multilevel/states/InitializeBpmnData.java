package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.models.generation.multilevel.options.BpmnMultiLevelGenerationModelOptions;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import reactor.core.publisher.Mono;

import static org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders.GLOBAL_VARIABLE_LIBRARY;
import static org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders.PROMPT_RELEVANT_GLOBAL_VARIABLES;

public class InitializeBpmnData extends ExecuteLogic {
    private final BpmnGlobalVariableLibrary globalVariableLibrary;
    private final BpmnMultiLevelGenerationModelOptions options;

    public InitializeBpmnData(BpmnGlobalVariableLibrary globalVariableLibrary, BpmnMultiLevelGenerationModelOptions options) {
        super(InitializeBpmnData.class);
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

        return Mono.just(Result.Ok());
    }
}
