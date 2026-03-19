package dmn.component;

import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

public class DmnComponentLibraryHighLevelSerializer implements ComponentLibrarySerializer<DmnComponentLibrary> {
    @Override
    public String serialize(DmnComponentLibrary library) {
        return library.defaultSerialize();
    }
}
