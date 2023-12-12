package org.rj.modelgen.llm.context.provider;

import org.rj.modelgen.llm.context.Context;

public interface ContextProvider {

    Context newContext();

    Context withPrompt(Context currentContext, String prompt);

}
