package org.rj.modelgen.llm.models.generation.multilevel.config;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.component.ComponentLibrarySelector;
import org.rj.modelgen.llm.component.ComponentLibrarySerializer;
import org.rj.modelgen.llm.intrep.assets.IntermediateModelAssets;
import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareAndSubmitMLRequestForLevelParams;
import org.rj.modelgen.llm.schema.ModelSchema;
import org.rj.modelgen.llm.statemodel.states.common.SubmitDetailLevelGenerationRequestToLlm;
import org.rj.modelgen.llm.statemodel.states.common.ValidateLlmIntermediateModelResponse;
import org.rj.modelgen.llm.statemodel.states.common.impl.PrepareSpecificModelGenerationRequestPromptWithComponents;
import org.rj.modelgen.llm.validation.impl.IntermediateModelSanitizer;

import java.util.function.Function;

public class MultiLevelModelDetailPhaseConfig<TIntermediateModel extends IntermediateModel, TIntermediateModelAssets extends IntermediateModelAssets,
                                        TComponentLibrary extends ComponentLibrary<?>,
                                        TPrepareImpl extends PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                        TSubmitImpl extends SubmitDetailLevelGenerationRequestToLlm,
                                        TValidateImpl extends ValidateLlmIntermediateModelResponse>

        extends MultiLevelModelPhaseConfig<TIntermediateModel, TIntermediateModelAssets, TComponentLibrary, TPrepareImpl, TSubmitImpl, TValidateImpl> {

    public MultiLevelModelDetailPhaseConfig(Class<TIntermediateModel> intermediateModelClass, Class<TIntermediateModelAssets> intermediateModelAssetsClass,
                                            ModelSchema modelSchema,
                                            IntermediateModelSanitizer<TIntermediateModel> modelSanitizer,
                                            ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                                            ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer,
                                            Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, TComponentLibrary, TPrepareImpl, ?, ?>, TPrepareImpl> customPrepareImplementation,
                                            Function<PrepareAndSubmitMLRequestForLevelParams<?, ?, ?, ?, TSubmitImpl, ?>, TSubmitImpl> customSubmitImplementation) {
        super(intermediateModelClass, intermediateModelAssetsClass, modelSchema, modelSanitizer, componentLibrarySelector, componentLibrarySerializer, customPrepareImplementation, customSubmitImplementation);
    }

    /**
     * Basic config without custom implementation classes for prepare & submit phases
     * @param <TIntermediateModel>
     * @param <TComponentLibrary>
     */
    public static class Basic<TIntermediateModel extends IntermediateModel, TIntermediateModelAssets extends IntermediateModelAssets, TComponentLibrary extends ComponentLibrary<?>>
            extends MultiLevelModelDetailPhaseConfig<TIntermediateModel, TIntermediateModelAssets, TComponentLibrary, PrepareSpecificModelGenerationRequestPromptWithComponents<TComponentLibrary>,
                                                     SubmitDetailLevelGenerationRequestToLlm, ValidateLlmIntermediateModelResponse> {

        public Basic(Class<TIntermediateModel> intermediateModelClass, Class<TIntermediateModelAssets> intermediateModelAssetsClass,
                     ModelSchema modelSchema,
                     IntermediateModelSanitizer<TIntermediateModel> modelSanitizer,
                     ComponentLibrarySelector<TComponentLibrary> componentLibrarySelector,
                     ComponentLibrarySerializer<TComponentLibrary> componentLibrarySerializer) {
            super(intermediateModelClass, intermediateModelAssetsClass, modelSchema, modelSanitizer, componentLibrarySelector, componentLibrarySerializer, null, null);
        }
    }
}
