package org.rj.modelgen.llm.models.generation.multilevel.config;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;

public class MultilevelModelPreprocessingConfig<TComponentLibrary extends ComponentLibrary<?>> {
    private ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector;
    private ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer;

    public MultilevelModelPreprocessingConfig() { }

    public MultilevelModelPreprocessingConfig(ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                              ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this.componentLibrarySelector = componentLibrarySelector;
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    public static <TComponentLibrary extends ComponentLibrary<?>> MultilevelModelPreprocessingConfig<TComponentLibrary> defaultConfig() {
        return new MultilevelModelPreprocessingConfig<>();
    }

    public ComponentLibrarySelector<TComponentLibrary> getComponentLibrarySelector() {
        return componentLibrarySelector;
    }

    public void setComponentLibrarySelector(ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector) {
        this.componentLibrarySelector = componentLibrarySelector;
    }

    public ComponentLibrarySerializer<TComponentLibrary> getComponentLibrarySerializer() {
        return componentLibrarySerializer;
    }

    public void setComponentLibrarySerializer(ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this.componentLibrarySerializer = componentLibrarySerializer;
    }
}
