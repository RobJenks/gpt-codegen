package org.rj.modelgen.llm.intrep;

import org.rj.modelgen.llm.util.Result;

public abstract class ModelParser<TModel> {

    public abstract Result<TModel, String> parse(String content);
}
