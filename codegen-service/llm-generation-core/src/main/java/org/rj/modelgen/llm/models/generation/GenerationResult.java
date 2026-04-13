package org.rj.modelgen.llm.models.generation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.intrep.assets.IntermediateModelAssets;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;
import org.rj.modelgen.llm.state.ModelInterfaceExecutionResult;
import org.rj.modelgen.llm.state.ModelInterfaceState;

import java.util.List;
import java.util.Optional;

public abstract class GenerationResult<TIntermediateModel extends IntermediateGraphModel<?, ?, ?, ?>, TIntermediateModelAssets extends IntermediateModelAssets> {
    private boolean successful;
    private TIntermediateModel intermediateModel;
    private TIntermediateModelAssets intermediateModelAssets;
    private List<String> validationMessages;
    private ModelInterfaceExecutionResult executionResults;

    public GenerationResult() {

    }

    public GenerationResult(boolean successful, TIntermediateModel intermediateModel, TIntermediateModelAssets intermediateModelAssets,
                            List<String> validationMessages, ModelInterfaceExecutionResult executionResults) {
        this.successful = successful;
        this.intermediateModel = intermediateModel;
        this.intermediateModelAssets = intermediateModelAssets;
        this.validationMessages = validationMessages;
        this.executionResults = executionResults;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public TIntermediateModel getIntermediateModel() {
        return intermediateModel;
    }

    public TIntermediateModelAssets getIntermediateModelAssets() {
        return intermediateModelAssets;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public ModelInterfaceExecutionResult getExecutionResults() {
        return executionResults;
    }

    @JsonIgnore
    public Optional<String> getLastError() {
        return Optional.ofNullable(executionResults)
                .map(ModelInterfaceExecutionResult::getResult)
                .map(ModelInterfaceState::getLastError);
    }
}
