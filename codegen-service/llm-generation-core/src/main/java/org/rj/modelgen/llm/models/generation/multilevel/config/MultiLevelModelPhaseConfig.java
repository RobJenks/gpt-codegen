package org.rj.modelgen.llm.models.generation.multilevel.config;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class MultiLevelModelPhaseConfig<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>> {
    private Class<TIntermediateModel> intermediateModelClass;
    private ModelSchema modelSchema;
    private IntermediateModelSanitizer<TIntermediateModel> modelSanitizer;
    private ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector;
    private ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer;

    public MultiLevelModelPhaseConfig() { }

    public MultiLevelModelPhaseConfig(Class<TIntermediateModel> intermediateModelClass, ModelSchema modelSchema,
                                      IntermediateModelSanitizer<TIntermediateModel> modelSanitizer,
                                      ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                      ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this.intermediateModelClass = intermediateModelClass;
        this.modelSchema = modelSchema;
        this.modelSanitizer = modelSanitizer;
        this.componentLibrarySelector = componentLibrarySelector;
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    public Class<TIntermediateModel> getIntermediateModelClass() {
        return intermediateModelClass;
    }

    public void setIntermediateModelClass(Class<TIntermediateModel> intermediateModelClass) {
        this.intermediateModelClass = intermediateModelClass;
    }

    public ModelSchema getModelSchema() {
        return modelSchema;
    }

    public void setModelSchema(ModelSchema modelSchema) {
        this.modelSchema = modelSchema;
    }

    public IntermediateModelSanitizer<TIntermediateModel> getModelSanitizer() {
        return modelSanitizer;
    }

    public void setModelSanitizer(IntermediateModelSanitizer<TIntermediateModel> modelSanitizer) {
        this.modelSanitizer = modelSanitizer;
    }

    public ComponentLibrarySelector<TComponentLibrary> getComponentLibrarySelector() {
        return componentLibrarySelector;
    }

    public ComponentLibrarySerializer<TComponentLibrary> getComponentLibrarySerializer() {
        return componentLibrarySerializer;
    }

    public void setComponentLibrarySerializer(ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this.componentLibrarySerializer = componentLibrarySerializer;
    }
}
