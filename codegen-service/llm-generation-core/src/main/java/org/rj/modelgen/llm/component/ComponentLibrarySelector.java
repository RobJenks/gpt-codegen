package org.rj.modelgen.llm.component;

import org.rj.modelgen.llm.state.ModelInterfacePayload;

public interface ComponentLibrarySelector<TComponentLibrary extends ComponentLibrary<?>> {

    TComponentLibrary getFilteredLibrary(TComponentLibrary baseLibrary, ModelInterfacePayload payload);

}
