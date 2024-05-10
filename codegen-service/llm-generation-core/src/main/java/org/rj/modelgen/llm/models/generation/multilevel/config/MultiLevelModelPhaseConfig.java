package org.rj.modelgen.llm.models.generation.multilevel.config;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.validation.IntermediateModelSanitizer;

public class MultiLevelModelPhaseConfig<TIntermediateModel extends IntermediateModel, TComponent extends Component> {
    private Class<TIntermediateModel> intermediateModelClass;
    private ModelSchema modelSchema;
    private IntermediateModelSanitizer modelSanitizer;
    private ComponentLibrarySerializer<TComponent> componentLibrarySerializer;

    public MultiLevelModelPhaseConfig() { }

    public MultiLevelModelPhaseConfig(Class<TIntermediateModel> intermediateModelClass, ModelSchema modelSchema,
                                      IntermediateModelSanitizer modelSanitizer, ComponentLibrarySerializer<TComponent> componentLibrarySerializer) {
        this.intermediateModelClass = intermediateModelClass;
        this.modelSchema = modelSchema;
        this.modelSanitizer = modelSanitizer;
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

    public IntermediateModelSanitizer getModelSanitizer() {
        return modelSanitizer;
    }

    public void setModelSanitizer(IntermediateModelSanitizer modelSanitizer) {
        this.modelSanitizer = modelSanitizer;
    }

    public ComponentLibrarySerializer<TComponent> getComponentLibrarySerializer() {
        return componentLibrarySerializer;
    }

    public void setComponentLibrarySerializer(ComponentLibrarySerializer<TComponent> componentLibrarySerializer) {
        this.componentLibrarySerializer = componentLibrarySerializer;
    }
}
