package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.models.generation.base.signals.BpmnGenerationSignals;
import org.rj.modelgen.bpmn.models.generation.multilevel.BpmnMultiLevelGenerationModel;
import org.rj.modelgen.bpmn.models.generation.validation.ValidateBpmnModel;
import org.rj.modelgen.llm.exception.LlmGenerationModelException;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.validation.beans.IntermediateModelValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.rj.modelgen.bpmn.models.generation.base.context.BpmnPromptPlaceholders.DETAIL_MODEL_VALIDATION_ISSUES;

public class ValidateBpmnLlmDetailLevelIntermediateModelResponse extends ModelInterfaceState implements CommonStateInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateBpmnLlmDetailLevelIntermediateModelResponse.class);

    public ValidateBpmnLlmDetailLevelIntermediateModelResponse() {
        super(ValidateBpmnLlmDetailLevelIntermediateModelResponse.class);
    }

    @Override
    public String getDescription() {
        return "Check output of the detail-level phase and determine whether to iterate or proceed to model generation";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal modelInterfaceSignal) {
        final String content = getPayload().get(MultiLevelModelStandardPayloadData.DetailLevelModel);
        final var parser = new IntermediateModelParser<>(BpmnIntermediateModel.class);

        final var model = parser.parse(content).orElseThrow(e -> new LlmGenerationModelException(String.format(
                "Validate BPMN Detail Level Intermediate Model Response could not parse detail-level intermediate model: %s (content: %s)", e, content)));

        final var componentLibrary = getComponentLibrary();

        List<IntermediateModelValidationError> validations = new ValidateBpmnModel(model, componentLibrary).validate();
        List<String> validationMessages = new ArrayList<>();
        validations.stream().collect(Collectors.groupingBy(IntermediateModelValidationError::getLocation))
                .forEach((nodeName, nodeValidations) -> {
                    String message = String.format("Node Name: %s, Issues to resolve: %s", nodeName,
                            nodeValidations.stream().map(IntermediateModelValidationError::getError).collect(Collectors.joining(", ")));
                    LOG.warn(message);
                    validationMessages.add(message);
                });

        // return outbound signal with validation results
        if (validations.isEmpty()) {
            LOG.info("BPMN detail-level intermediate model is valid, proceeding to next phase");
            return outboundSignal(getSuccessSignalId()).mono();
        } else {
            LOG.info("BPMN detail-level intermediate model is invalid, returning to detail-level phase for corrections");
            return outboundSignal(BpmnGenerationSignals.IntermediateModelIsInvalid)
                    .withPayloadData(DETAIL_MODEL_VALIDATION_ISSUES.getValue(), validationMessages)
                    .mono();
        }
    }

    private BpmnComponentLibrary getComponentLibrary() {
        return Optional.ofNullable(getModel())
                .map(m -> m.getAs(BpmnMultiLevelGenerationModel.class))
                .map(BpmnMultiLevelGenerationModel::getComponentLibrary)
                .orElse(null);
    }
}
