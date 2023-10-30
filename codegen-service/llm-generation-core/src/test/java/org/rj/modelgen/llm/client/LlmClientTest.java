package org.rj.modelgen.llm.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.integrations.openai.OpenAIClientConfig;

public class LlmClientTest {

    @Test
    public void testCreatingClient() {
        final var config = new OpenAIClientConfig(() -> "abc");
        final var client = new LlmClientImpl<>(config);

        Assertions.assertNotNull(client);
    }

}
