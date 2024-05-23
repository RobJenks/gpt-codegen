package org.rj.modelgen.llm.component;

public interface ComponentLibrarySerializer<TComponentLibrary extends ComponentLibrary<?>> {

    String serialize(TComponentLibrary library);

}
