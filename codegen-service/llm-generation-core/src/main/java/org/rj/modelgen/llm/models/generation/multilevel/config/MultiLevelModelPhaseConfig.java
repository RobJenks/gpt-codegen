package org.rj.modelgen.llm.models.generation.multilevel.config;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevel;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.SubmitGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.validation.impl.IntermediateModelSanitizer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiLevelModelPhaseConfig<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>,
                                        TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                        TSubmitImpl extends SubmitGenerationRequestToLlm,
                                        TValidateImpl extends ValidateLlmIntermediateModelResponse> {

    /**
     * Basic config without custom implementation classes for prepare & submit phases
     * @param <TIntermediateModel>
     * @param <TComponentLibrary>
     */
    public static class Basic<TIntermediateModel extends IntermediateModel, TComponentLibrary extends ComponentLibrary<?>>
            extends MultiLevelModelPhaseConfig<TIntermediateModel, TComponentLibrary, PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>, SubmitGenerationRequestToLlm, ValidateLlmIntermediateModelResponse> {

        public Basic(Class<TIntermediateModel> intermediateModelClass, ModelSchema modelSchema,
                                          IntermediateModelSanitizer<TIntermediateModel> modelSanitizer,
                                          ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                          ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
            super(intermediateModelClass, modelSchema, modelSanitizer, componentLibrarySelector, componentLibrarySerializer, null, null);
        }
    }

    private Class<TIntermediateModel> intermediateModelClass;
    private ModelSchema modelSchema;
    private IntermediateModelSanitizer<TIntermediateModel> modelSanitizer;
    private ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector;
    private ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer;
    private Function<PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?>, TPrepareImpl> customPrepareImplementation;
    private Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>, TSubmitImpl> customSubmitImplementation;
    private BiFunction<ModelSchema, Class<TIntermediateModel>, TValidateImpl> customValidateImplementation;

    public MultiLevelModelPhaseConfig() { }


    public MultiLevelModelPhaseConfig(Class<TIntermediateModel> intermediateModelClass, ModelSchema modelSchema,
                                      IntermediateModelSanitizer<TIntermediateModel> modelSanitizer,
                                      ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                      ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer,
                                      Function<PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?>, TPrepareImpl> customPrepareImplementation,
                                      Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>, TSubmitImpl> customSubmitImplementation) {
        this.intermediateModelClass = intermediateModelClass;
        this.modelSchema = modelSchema;
        this.modelSanitizer = modelSanitizer;
        this.componentLibrarySelector = componentLibrarySelector;
        this.componentLibrarySerializer = componentLibrarySerializer;
        this.customPrepareImplementation = customPrepareImplementation;
        this.customSubmitImplementation = customSubmitImplementation;
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

    public void setComponentLibrarySelector(ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector) {
        this.componentLibrarySelector = componentLibrarySelector;
    }

    public ComponentLibrarySerializer<TComponentLibrary> getComponentLibrarySerializer() {
        return componentLibrarySerializer;
    }

    public void setComponentLibrarySerializer(ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
        this.componentLibrarySerializer = componentLibrarySerializer;
    }

    public Function<PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?>, TPrepareImpl> getCustomPrepareImplementation() {
        return customPrepareImplementation;
    }

    public void setCustomPrepareImplementation(Function<PrepareAndSubmitMLRequestForLevelParams<?, TComponentLibrary, TPrepareImpl, ?, ?>, TPrepareImpl> customPrepareImplementation) {
        this.customPrepareImplementation = customPrepareImplementation;
    }

    public Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>, TSubmitImpl> getCustomSubmitImplementation() {
        return customSubmitImplementation;
    }

    public void setCustomSubmitImplementation(Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, TSubmitImpl, ?>, TSubmitImpl> customSubmitImplementation) {
        this.customSubmitImplementation = customSubmitImplementation;
    }

    public ValidateLlmIntermediateModelResponse createValidationStage(ModelSchema modelSchema, Class<TIntermediateModel> intermediateModelClass) {
        return Optional.ofNullable(customValidateImplementation)
                .map(fn -> (ValidateLlmIntermediateModelResponse)fn.apply(modelSchema, intermediateModelClass))
                .orElseGet(() -> new ValidateLlmIntermediateModelResponse(modelSchema, intermediateModelClass));
    }
}
