package org.rj.modelgen.llm.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelRequest;
import org.rj.modelgen.llm.integrations.openai.OpenAIModelResponse;
import org.rj.modelgen.llm.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionState {
    public static final SessionState NONE = new SessionState("<no-session>", ExecutionContext.None);

    private final String id;
    private ExecutionContext executionContext;
    private final List<ContextEntry> events;
    private Integer totalTokensUsed = 0;
    private Integer estimatedCompressedTokenSize = 0;
    private Integer estimatedUncompressedTokenSize = 0;
    private String lastPrompt;
    private String lastResponse;
    private Boolean validOutput;
    private List<String> validationErrors;
    private Integer iterationsRequired;
    private Float currentTemperature;
    private String transformedContent;

    public SessionState(String id, ExecutionContext executionContext) {
        this.id = id;
        this.executionContext = executionContext;
        this.events = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public List<ContextEntry> getEvents() {
        return events;
    }

    @JsonIgnore
    public void addEvent(ContextEntry event) {
        this.events.add(event);
    }

    public Integer getTotalTokensUsed() {
        return totalTokensUsed;
    }
    @JsonIgnore
    public void addTotalTokensUsed(int tokens) {
        this.totalTokensUsed += tokens;
    }

    public Integer getEstimatedUncompressedTokenSize() {
        return estimatedUncompressedTokenSize;
    }

    @JsonIgnore
    public void estimateNewUncompressedTokenSize(int newUncompressedDelta) {
        // Would be submitting entire context again, along with the delta
        this.estimatedUncompressedTokenSize +=
                (estimatedUncompressedTokenSize + newUncompressedDelta);
    }

    public Integer getEstimatedCompressedTokenSize() {
        return estimatedCompressedTokenSize;
    }

    @JsonIgnore
    public void estimateNewCompressedTokenSize(int estimatedCompressedTokenSize) {
        this.estimatedCompressedTokenSize += estimatedCompressedTokenSize;
    }

    @JsonIgnore
    public void addEstimatedTokensForPrompt(OpenAIModelRequest prompt) {
        // Uncompressed submission would need to re-submit all events so far, plus the new user prompt
        // If this is the first prompt (!hasLastResponse) then DO include the assistant tokens since we have to supply them on first request
        this.estimatedUncompressedTokenSize +=
                (this.estimatedUncompressedTokenSize + prompt.estimateTokenSize(!hasLastResponse()));

        // Compressed submission only submits the previous assistant response, plus the new user prompt (i.e. the content of `body`)
        this.estimatedCompressedTokenSize += prompt.estimateTokenSize(true);
    }

    @JsonIgnore
    public void addEstimatedTokensForResponse(OpenAIModelResponse response) {
        // Both compressed and uncompressed scenarios will incur the same token cost for the response
        this.estimatedCompressedTokenSize += response.getUsage().getCompletion_tokens();
        this.estimatedUncompressedTokenSize += response.getUsage().getCompletion_tokens();
    }

    public String getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(String lastResponse) {
        this.lastResponse = lastResponse;
    }

    @JsonIgnore
    public boolean hasLastResponse() {
        return lastResponse != null;
    }

    public String getLastPrompt() {
        return lastPrompt;
    }

    public void setLastPrompt(String lastPrompt) {
        this.lastPrompt = lastPrompt;
    }

    public Boolean getValidOutput() {
        return validOutput;
    }

    public void setValidOutput(Boolean validOutput) {
        this.validOutput = validOutput;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    @JsonIgnore
    public boolean hasValidationErrors() {
        return Optional.ofNullable(validationErrors).map(x -> !x.isEmpty()).orElse(false);
    }

    public void setValidationErrors(List<String> validationErrors) {
        if (validationErrors == null) return;
        this.validationErrors = new ArrayList<>(validationErrors);
    }

    @JsonIgnore
    public void addValidationError(String validationError) {
        if (this.validationErrors.isEmpty()) {
            this.validationErrors = new ArrayList<>();
        }

        this.validationErrors.add(validationError);
    }

    public Integer getIterationsRequired() {
        return iterationsRequired;
    }

    public void setIterationsRequired(Integer iterationsRequired) {
        this.iterationsRequired = iterationsRequired;
    }

    public Float getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Float currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getTransformedContent() {
        return transformedContent;
    }

    public void setTransformedContent(String transformedContent) {
        this.transformedContent = transformedContent;
    }

    @JsonIgnore
    public void controlTokensResolved() {
        events.stream()
                .filter(x -> x.getContent() != null)
                .forEach(x -> x.setContent(x.getContent().replaceAll(Constants.PATTERN_LOAD.toString(), "")));

        if (lastPrompt != null) {
            lastPrompt = lastPrompt.replaceAll(Constants.PATTERN_LOAD.toString(), "");
        }
    }
}
