package org.rj.modelgen.bpmn.models.generation.validation;

import org.apache.commons.lang3.StringUtils;
import org.rj.modelgen.bpmn.component.BpmnComponent;
import org.rj.modelgen.bpmn.component.BpmnComponentLibrary;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariable;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.*;

public class BpmnScriptUtils {

    public static List<PayloadVariable> retrieveReadVariables(String inputValue) {
        List<PayloadVariable> variables = new ArrayList<>();
        Matcher matcher = VAR_READ_PATTERN.matcher(inputValue);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            variables.add(new PayloadVariable(variableName, "string"));
        }
        return variables;
    }

    public static List<PayloadVariable> retrieveWriteVariables(String inputValue) {
        List<PayloadVariable> variables = new ArrayList<>();
        Matcher matcher = VAR_WRITE_PATTERN.matcher(inputValue);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = matcher.group(2);
            String variableType = matcher.group(3);
            variables.add(new PayloadVariable(variableName, variableType.toLowerCase()));
        }
        return variables;
    }

    public static List<PayloadVariable> retrieveGlobalVariables(String inputValue, BpmnGlobalVariableLibrary globalVariableLibrary) {
        List<PayloadVariable> variables = new ArrayList<>();
        Matcher matcher = GLOBAL_VAR_READ_PATTERN.matcher(inputValue);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableType = globalVariableLibrary.getVariableByName(variableName).map(BpmnGlobalVariable::getType).orElse("");
            variables.add(new PayloadVariable(variableName, variableType.toLowerCase()));
        }
        return variables;
    }

    // Resolves variable reads as payload variables. It ignores the source node ID and simply replaces the variable name with "payload.variableName".
    public static String resolveVariableReadsAsPayloadVar(String inputValue, boolean withInterpolation) {
        String replacement = withInterpolation ? "\\${payload.$1}" : "payload.$1";
        return inputValue.replaceAll(VAR_READ_PATTERN.pattern(), replacement);
    }

    // Resolves variable reads as payload variables or component outputs.
    public static String resolveVariableReads(String inputValue, boolean withInterpolation, BpmnIntermediateModel model, BpmnComponentLibrary componentLibrary) {
        StringBuilder resolved = new StringBuilder();
        Matcher matcher = VAR_READ_PATTERN.matcher(inputValue);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableSource = matcher.groupCount() >= 2 ? matcher.group(2) : null;

            String replacement = withInterpolation
                    ? "${payload." + variableName + "}"
                    : "payload." + variableName;

            // If source node Id is provided, check if the variable is defined in a component's generated outputs
            if (StringUtils.isNotBlank(variableSource)) {
                var resolveValue = model.getNodeById(variableSource)
                        .flatMap(node -> componentLibrary.getComponentByName(node.getElementType()))
                        .flatMap(component -> component.getGeneratedOutputs().stream()
                                    .filter(outputVar -> outputVar.getName().equals(variableName))
                                    .findFirst()
                                    .map(BpmnComponent.Variable::getResolveValue));
                if (resolveValue.isPresent()) {
                    replacement = withInterpolation
                        ? "${" + resolveValue.get() + "}"
                        : resolveValue.get();
                }
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    public static String resolveVariableWrites(String inputValue) {
        return inputValue.replaceAll(VAR_WRITE_PATTERN.pattern(), "payload.$1 = $2");
    }

    public static String resolveErrorThrows(String inputValue, String replacement) {
        return inputValue.replaceAll(THROW_ERROR_PATTERN.pattern(), replacement);
    }

    public static String resolveGlobalVariableReads(String inputValue, BpmnGlobalVariableLibrary globalVariableLibrary, boolean withInterpolation) {
        Matcher globalVarMatcher = GLOBAL_VAR_READ_PATTERN.matcher(inputValue);
        StringBuilder resolvedScript = new StringBuilder();
        while (globalVarMatcher.find()) {
            String varName = globalVarMatcher.group(1);
            String arguments = globalVarMatcher.group(2);

            var globalVar = globalVariableLibrary.getVariableByName(varName);
            if (globalVar.isPresent()) {
                String resolveValue = globalVar.get().getResolveValue();

                // Replace argument placeholders with actual arguments
                if (arguments != null && !arguments.trim().isEmpty()) {
                    String[] args = arguments.split(",");
                    for (int i = 0; i < args.length; i++) {
                        resolveValue = resolveValue.replace("arg" + (i + 1), args[i].trim());
                    }
                }
                if (withInterpolation) {
                    globalVarMatcher.appendReplacement(resolvedScript, "${" + resolveValue + "}");
                } else {
                    globalVarMatcher.appendReplacement(resolvedScript, resolveValue);
                }

            }
        }
        globalVarMatcher.appendTail(resolvedScript);
        return resolvedScript.toString();
    }

    public static String stripInterpolationSyntax(String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            return expression.substring(2, expression.length() - 1); // Remove ${ and }
        }
        return expression;
    }

    public static String stripQuotes(String expression) {
        expression = expression.strip();
        if ((expression.startsWith("\"") && expression.endsWith("\"")) || (expression.startsWith("'") && expression.endsWith("'"))) {
            return expression.substring(1, expression.length() - 1);
        }
        return expression;
    }
}