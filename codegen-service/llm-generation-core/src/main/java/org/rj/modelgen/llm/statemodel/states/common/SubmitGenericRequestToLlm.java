package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.statemodel.signals.common.CommonStateInterface;
import org.rj.modelgen.llm.validation.impl.GenericModelResponseSanitizer;

public class SubmitGenericRequestToLlm extends SubmitGenerationRequestToLlm implements CommonStateInterface {
    public SubmitGenericRequestToLlm() {
        super(SubmitGenericRequestToLlm.class, new GenericModelResponseSanitizer());
    }
}
