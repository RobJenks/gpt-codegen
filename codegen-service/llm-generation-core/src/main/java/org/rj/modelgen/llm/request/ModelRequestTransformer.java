package org.rj.modelgen.llm.request;

public interface ModelRequestTransformer<T> {
    T transform(ModelRequest request);
}
