package dmn.component;

import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

public class DmnComponentLibraryDetailLevelSerializer implements ComponentLibrarySerializer<DmnComponentLibrary> {
    @Override
    public String serialize(DmnComponentLibrary library) {
        return library.defaultSerialize();
    }
}

