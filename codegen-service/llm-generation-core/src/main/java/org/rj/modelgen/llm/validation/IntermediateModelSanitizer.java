package org.rj.modelgen.llm.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class IntermediateModelSanitizer {
    private static final Pattern JSON_EXTRACT = Pattern.compile("^.*?(\\{.*}).*?$", Pattern.DOTALL | Pattern.MULTILINE);

    public String sanitize(String content) {
        // Basic sanitizing; attempt to locate the largest JSON block within the output, in case of additional text
        // in violation of prompt constraints

        if (StringUtils.isBlank(content)) return content;

        // In case the model has ignored the "only the JSON" constraint, attempt to locate the largest JSON block within the response
        final var matcher = JSON_EXTRACT.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return content;
    }
}
