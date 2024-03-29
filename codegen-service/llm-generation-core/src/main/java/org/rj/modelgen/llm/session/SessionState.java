package org.rj.modelgen.llm.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.request.ModelRequest;
import org.rj.modelgen.llm.response.ModelResponse;
import org.rj.modelgen.llm.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SessionState {
    public static final SessionState NONE = new SessionState("<no-session>");

    private final String id;
    private Context context;
    private Integer totalTokensUsed = 0;
    private Integer estimatedCompressedTokenSize = 0;
    private Integer estimatedUncompressedTokenSize = 0;
    private Integer iterationsRequired;
    private Double currentTemperature;
    private Integer userPromptCount = 0;
    private Integer modelResponseCount = 0;
    private Map<String, Object> metadata;

    public SessionState(String id) {
        this.id = id;
        this.context = null;
        this.metadata = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public Context getContext() {
        return context;
    }

    public Context getOrCreateContext(Supplier<Context> contextGenerator) {
        if (context == null) {
            context = contextGenerator.get();
        }

        return context;
    }
    public void replaceContext(Context context) {
        this.context = context;
    }

    public void recordUserPrompt(ModelRequest modelRequest) {
        // TODO
        userPromptCount++;
    }
    public void recordModelResponse(ModelResponse response) {
        // TODO
        modelResponseCount++;
    }

    @JsonIgnore
    public boolean isNewSession() {
        return getUserPromptCount() == 0 && getModelResponseCount() == 0;
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
    public void addEstimatedTokensForPrompt(ModelRequest prompt) {
        // Uncompressed submission would need to re-submit all events so far, plus the new user prompt
        // If this is the first prompt (!hasLastResponse) then DO include the assistant tokens since we have to supply them on first request
        this.estimatedUncompressedTokenSize +=
                (this.estimatedUncompressedTokenSize + prompt.estimateTokenSize(!context.hasLatestModelEntry()));

        // Compressed submission only submits the previous assistant response, plus the new user prompt (i.e. the content of `body`)
        this.estimatedCompressedTokenSize += prompt.estimateTokenSize(true);
    }

    @JsonIgnore
    public void addEstimatedTokensForResponse(ModelResponse response) {
        // Both compressed and uncompressed scenarios will incur the same token cost for the response
        this.estimatedCompressedTokenSize += response.getResponseTokenUsage();
        this.estimatedUncompressedTokenSize += response.getResponseTokenUsage();
    }

    public Optional<String> getLastResponse() {
        return context.getLatestModelEntry().map(ContextEntry::getContent);
    }

    public Optional<String> getLastPrompt() {
        return context.getLatestUserEntry().map(ContextEntry::getContent);
    }

    public Integer getIterationsRequired() {
        return iterationsRequired;
    }

    public void setIterationsRequired(Integer iterationsRequired) {
        this.iterationsRequired = iterationsRequired;
    }

    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public Integer getUserPromptCount() {
        return userPromptCount;
    }

    public Integer getModelResponseCount() {
        return modelResponseCount;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @JsonIgnore
    public void controlTokensResolved() {
        context.getData().stream()
                .filter(x -> x.getContent() != null)
                .forEach(x -> x.setContent(x.getContent().replaceAll(Constants.PATTERN_LOAD.toString(), "")));
    }
}
