package org.rj.modelgen.llm.prompt;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplatedPromptGenerator<TImpl extends TemplatedPromptGenerator<?, TSelector>, TSelector> extends PromptGenerator<TImpl, TSelector> {
    private final Map<TSelector, String> prompts;

    public TemplatedPromptGenerator() {
        this(Map.of());
    }

    public TemplatedPromptGenerator(Map<TSelector, String> prompts) {
        this.prompts = new HashMap<>(prompts);
    }

    @Override
    public TImpl withAvailablePrompt(TSelector selector, String prompt) {
        return super.withAvailablePrompt(selector, prompt);
    }

    @Override
    public Optional<String> getPrompt(TSelector selector, List<PromptSubstitution> substitutions) {
        return Optional.ofNullable(selector)
                .map(prompts::get)
                .map(raw -> {
                    Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
                    configuration.setClassForTemplateLoading(configuration.getClass(), "/");
                    try {
                        Template template = new Template("template", new StringReader(raw), configuration);
                        StringWriter output = new StringWriter();
                        template.process(createDataModel(parameters), output);
                        return output.toString();
                    } catch (Exception ex) {
                        return null;
                    }
            });
    }

  private Map<String, Object> createDataModel(List<PromptSubstitution> parameters) {
    Map<String, Object> dataModel = new HashMap<>();
    for (PromptSubstitution parameter: parameters) {
        dataModel.put(parameter.getExistingString(), parameter.getNewString());
    }
    return dataModel;
  }
}