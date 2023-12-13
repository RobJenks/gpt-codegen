package org.rj.modelgen.llm.response;

public interface ModelResponseTransformer<T> {
    ModelResponse transform(T response);
}
