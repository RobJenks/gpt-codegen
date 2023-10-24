package org.rj.modelgen.llm.integrations.openai;

import org.rj.modelgen.llm.client.LlmClient;
import org.rj.modelgen.llm.client.LlmClientImpl;
import org.rj.modelgen.llm.model.ModelInterface;

import java.util.Optional;
import java.util.function.Supplier;

public class OpenAIModelInterface extends ModelInterface {
    private OpenAIModelInterface(LlmClient llmClient) {
        super(llmClient);
    }

    public static class Builder {
        private Supplier<String> apiKeyGenerator;
        public Builder() {
            this.apiKeyGenerator = null;
        }

        public Builder withApiKeyGenerator(Supplier<String> apiKeyGenerator) {
            this.apiKeyGenerator = apiKeyGenerator;
            return this;
        }

        public OpenAIModelInterface build() {
            final var keyGenerator = Optional.ofNullable(apiKeyGenerator).orElseGet(() -> (() -> null));

            final var llmClientConfig = new OpenAIClientConfig(keyGenerator);
            final var llmClient = new LlmClientImpl<>(llmClientConfig);

            return new OpenAIModelInterface(llmClient);
        }
    }
}
