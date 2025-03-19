package org.rj.modelgen.llm.component;

import java.util.Optional;

public class DefaultComponentLibrarySerializer<TComponentLibrary extends ComponentLibrary<?>> implements ComponentLibrarySerializer<TComponentLibrary> {
    @Override
    public String serialize(TComponentLibrary library) {
        return Optional.ofNullable(library)
                .map(ComponentLibrary::defaultSerialize)
                .orElse("<null>");
    }
}
