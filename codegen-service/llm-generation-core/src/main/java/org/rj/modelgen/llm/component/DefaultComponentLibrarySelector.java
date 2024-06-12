package org.rj.modelgen.llm.component;

import org.rj.modelgen.llm.state.ModelInterfacePayload;

public class DefaultComponentLibrarySelector<TComponentLibrary extends ComponentLibrary<?>> implements ComponentLibrarySelector<TComponentLibrary> {
    @Override
    public TComponentLibrary getFilteredLibrary(TComponentLibrary baseLibrary, ModelInterfacePayload payload) {
        return baseLibrary;
    }
}
