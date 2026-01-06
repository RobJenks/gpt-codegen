package org.rj.modelgen.bpmn.models.generation.multilevel.states;

import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariable;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeInput;
import org.rj.modelgen.bpmn.intrep.model.ElementNodeOutput;
import org.rj.modelgen.bpmn.models.generation.multilevel.BpmnMultiLevelGenerationModel;
import org.rj.modelgen.llm.models.generation.multilevel.data.MultiLevelModelStandardPayloadData;
import org.rj.modelgen.llm.models.generation.multilevel.states.PrepareModelForRendering;
import org.rj.modelgen.llm.util.Result;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.rj.modelgen.bpmn.component.common.BpmnComponentInputSourceType.*;
import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.*;
import static org.rj.modelgen.bpmn.models.generation.validation.BpmnScriptUtils.*;

public class PrepareBpmnModelForRendering extends PrepareModelForRendering {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareBpmnModelForRendering.class);

    private final BpmnGlobalVariableLibrary globalVariableLibrary;

    public PrepareBpmnModelForRendering(BpmnGlobalVariableLibrary globalVariableLibrary) {
        this(globalVariableLibrary, PrepareBpmnModelForRendering.class);
    }

    public PrepareBpmnModelForRendering(BpmnGlobalVariableLibrary globalVariableLibrary, Class<? extends PrepareModelForRendering> cls) {
        super(cls);
        this.globalVariableLibrary = globalVariableLibrary;
    }

    public PrepareBpmnModelForRendering withInputKeyOverride(String inputKeyOverride) {
        this.inputKeyOverride = inputKeyOverride;
        return this;
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        final var findModel = getModelData();
        if (findModel.isErr()) return Mono.just(Result.Err(findModel.getError()));
        final var model = findModel.getValue();

        final var componentLibrary = getComponentLibrary();
        if (componentLibrary == null) return Mono.just(Result.Err("Cannot prepare model for rendering; no component library available"));

        // Operations to be applied in order
        final List<Runnable> operations = List.of(
                () -> removeInvalidNullNodes(model),
                () -> eliminateDuplicateConnections(model),
                () -> identifyOrphanedSubgraphs(model),
                () -> resolveInputs(model)
        );

        return execute(model, operations);
    }

    // Identify any cases where a node is connected to another node via multiple edges, and remove the duplicate edges
    private void eliminateDuplicateConnections(BpmnIntermediateModel model) {
        for (final var node : model.getNodes()) {

            final var connections = node.getConnectedTo();
            // Make sure there are no duplicate connections within a group
            final Set<String> duplicates = new HashSet<>();
            final var distinctConnections = connections.stream()
                    .filter(conn -> duplicates.add(conn.getTargetNode()))
                    .toList();
            if (distinctConnections.size() != connections.size()) {
                node.setConnectedTo(distinctConnections);
            }
        }
    }

    private void resolveInputs(BpmnIntermediateModel model) {
        for (final var node : model.getNodes()) {
            if (node.getInputs() == null) continue;
            for (final var input : node.getInputs()) {
                var inputValue = input.getValue();
                var inputSource = input.getVariableSource();

                if (inputSource.equals(SCRIPT.toString())) {
                    inputValue = resolveVariableWrites(inputValue);
                    inputValue = resolveVariableReads(inputValue);
                    inputValue = resolveGlobalVariableReads(inputValue, globalVariableLibrary, false);

                    // Hook for subclasses to add custom post-processing
                    inputValue = postProcessScriptInput(input, inputValue);
                }

                if (inputSource.equals(EXPRESSION.toString())) {
                    inputValue = inputValue
                            .replaceAll(INTERPOLATION_PATTERN.pattern(), "\\${payload.$1}")
                            .replaceAll(VAR_READ_PATTERN.pattern(), "\\${payload.$1}");
                    inputValue = resolveGlobalVariableReads(inputValue, globalVariableLibrary, true);
                }

                if (inputSource.equals(GLOBAL.toString())) {
                    inputValue = globalVariableLibrary.getVariableByName(input.getValue())
                            .map(BpmnGlobalVariable::getResolveValue)
                            .orElse("global_not_found");
                }

                if (inputSource.equals(NODE.toString())) {
                    inputValue = model.getNodeById(inputValue)
                            .flatMap(x -> x.findOutput(input.getName()))
                            .map(ElementNodeOutput::getValue)
                            .orElse("node_not_found");
                }

                input.setValue(inputValue);
            }
        }
    }

    protected String postProcessScriptInput(ElementNodeInput input, String inputValue) {
        inputValue = resolveErrorThrows(inputValue, "throw new Exception($1)");
        return inputValue;
    }

    private String getModelKey() {
        return Optional.ofNullable(inputKeyOverride).orElse(MultiLevelModelStandardPayloadData.DetailLevelModel.toString());
    }

    private BpmnComponentLibrary getComponentLibrary() {
        return Optional.ofNullable(getModel())
                .map(m -> m.getAs(BpmnMultiLevelGenerationModel.class))
                .map(BpmnMultiLevelGenerationModel::getComponentLibrary)
                .orElse(null);
    }

    private Result<BpmnIntermediateModel, String> getModelData() {
        return Optional.ofNullable(getPayload().<String>get(getModelKey()))
                .map(serialized -> Util.tryDeserialize(serialized, BpmnIntermediateModel.class))
                .map(res -> res.mapErr(Exception::toString))
                .orElseGet(() -> Result.Err("No valid input model found"));
    }

    @Override
    public String getDescription() {
        return "Clean up BPMN model for rendering";
    }

}
