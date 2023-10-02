package org.rj.modelgen.llm.context.provider.impl;

import org.rj.modelgen.llm.context.Context;
import org.rj.modelgen.llm.context.ContextEntry;
import org.rj.modelgen.llm.context.provider.ContextProvider;

public class DefaultContextProvider implements ContextProvider {
    @Override
    public Context newContext() {
        return new Context();
    }

    @Override
    public Context withPrompt(Context currentContext, String prompt) {
        if (currentContext == null) return null;

        // Simply returns a new extended context
        final var newContext = currentContext.copy();
        newContext.addEntry(ContextEntry.forUser(prompt));

        return newContext;
    }
}
