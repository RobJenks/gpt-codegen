package org.rj.modelgen.bpmn.models.generation.validation;

import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariable;
import org.rj.modelgen.bpmn.component.globalvars.library.BpmnGlobalVariableLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.rj.modelgen.bpmn.generation.BpmnConstants.Patterns.*;

public class BpmnScriptUtils {

    public static List<PayloadVariable> retrieveVariablesWithPattern(String inputValue, Pattern pattern) {
        List<PayloadVariable> variables = new ArrayList<>();
        Matcher matcher = pattern.matcher(inputValue);
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

    public static String resolveVariableReads(String inputValue, boolean withInterpolation) {
        String replacement = withInterpolation ? "\\${payload.$1}" : "payload.$1";
        return inputValue.replaceAll(VAR_READ_PATTERN.pattern(), replacement);
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
}