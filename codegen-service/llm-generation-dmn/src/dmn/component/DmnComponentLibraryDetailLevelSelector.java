package dmn.component;

import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.state.ModelInterfacePayload;

public class DmnComponentLibraryDetailLevelSelector implements ComponentLibrarySelector<DmnComponentLibrary> {
    @Override
    public DmnComponentLibrary getFilteredLibrary(DmnComponentLibrary baseLibrary, ModelInterfacePayload payload) {
        // Expose the full DMN library in all stages
        return new DmnComponentLibrary(baseLibrary);
    }
}
