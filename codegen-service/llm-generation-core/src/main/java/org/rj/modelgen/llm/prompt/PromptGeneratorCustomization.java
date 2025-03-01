package org.rj.modelgen.llm.prompt;

import java.util.function.Consumer;

/**
 * Signature for a customization of a PromptGenerator, which can be passed into the model via its generation
 * options.  Allows per-instance customization of the model prompt generator by the caller.
 */
public interface PromptGeneratorCustomization//<TImpl extends PromptGenerator<?, TSelector>, TSelector,
        <TPromptGenerator extends PromptGenerator<?>>
        extends Consumer<TPromptGenerator> { }
