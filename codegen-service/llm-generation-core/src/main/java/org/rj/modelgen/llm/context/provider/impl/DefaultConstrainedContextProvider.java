package org.rj.modelgen.llm.context.provider.impl;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.ContextProvider;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DefaultConstrainedContextProvider implements ContextProvider {
    private final Function<Context, Context> constraintFunction;

    /**
     * Context provider which maintains a length-constrained context with each new user prompt.  Uses a default
     * constraint function
     */
    public DefaultConstrainedContextProvider() {
        this(DefaultConstrainedContextProvider::defaultConstraintFunction);
    }

    /**
     * Context provider which maintains a length-constrained context with each new user prompt.  Uses the provided constraint
     * function to generate the new constrained context
     * @param constraintFunction Accepts a reference to the current context and returns a constrained context
     */
    public DefaultConstrainedContextProvider(Function<Context, Context> constraintFunction) {
        this.constraintFunction = Optional.ofNullable(constraintFunction)
                                          .orElse(DefaultConstrainedContextProvider::defaultConstraintFunction);
    }

    @Override
    public Context newContext() {
        return new Context();
    }

    @Override
    public final Context withPrompt(Context currentContext, String prompt) {
        if (currentContext == null) return null;

        final var newContext = constraintFunction.apply(currentContext);
        return withPromptOnConstrainedContext(newContext, prompt);
    }

    public Context withPromptOnConstrainedContext(Context constrainedCurrentContext, String prompt) {
        if (constrainedCurrentContext == null) return null;

        // Default behavior if not overridden; simply append the new prompt to the given constrained context
        final var newContext = constrainedCurrentContext.copy();
        newContext.addEntry(ContextEntry.forUser(prompt));

        return newContext;
    }

    private static Context defaultConstraintFunction(Context currentContext) {
        if (currentContext == null) return null;

        // Return a new context containing only the most recent model response.  A new user prompt
        // will be appended to yield a context that is always fixed length [MODEL, USER]
        return currentContext.getLatestModelEntry()
                .map(List::of)
                .map(Context::new)

                // Return current context as a fallback if it cannot be shortened
                .orElse(currentContext);
    }
}
