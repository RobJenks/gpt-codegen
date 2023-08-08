package org.rj.modelgen.service.gpt.client;

import org.rj.modelgen.service.gpt.beans.PromptContextSubmission;
import org.rj.modelgen.service.gpt.beans.SubmissionResponse;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

public interface GptClient {

    static GptClient build(Environment environment) {
        if (environment == null) throw new IllegalArgumentException("Cannot build GPT client without valid environment");
        if (environment.getProperty("gptclient.type", "default").equals("mock")) {
            return new GptMockClientImpl(environment);
        }
        else {
            return new GptClientImpl(environment);
        }
    }

    Mono<SubmissionResponse> submit(PromptContextSubmission submission);

}
