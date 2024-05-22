package org.rj.modelgen.llm.component;

public interface ComponentLibrarySerializer<TComponent extends Component> {

    String serialize(ComponentLibrary<TComponent> library);
}
