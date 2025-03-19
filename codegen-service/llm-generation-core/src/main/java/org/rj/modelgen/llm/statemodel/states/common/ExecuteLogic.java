package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.prompt.TemplatedPromptGenerator;
import org.rj.modelgen.llm.state.ModelInterfaceSignal;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateType;
import org.rj.modelgen.llm.statemodel.signals.common.StandardErrorSignals;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.util.Result;
import reactor.core.publisher.Mono;

public abstract class ExecuteLogic<TPromptGenerator extends TemplatedPromptGenerator<TPromptGenerator>, TComponentLibrary extends ComponentLibrary<?>> extends ModelInterfaceState {
    private TPromptGenerator promptGenerator;
    private TComponentLibrary componentLibrary;

    public ExecuteLogic(TPromptGenerator promptGenerator, TComponentLibrary componentLibrary) {
        super(ExecuteLogic.class);
        this.promptGenerator = promptGenerator;
        this.componentLibrary = componentLibrary;
    }

    @Override
    public String getDescription() {
        return "Executes custom logic";
    }

    @Override
    protected Mono<ModelInterfaceSignal> invokeAction(ModelInterfaceSignal inputSignal) {
        return executeLogic()
                .flatMap(result -> result
                        .map(__ -> outboundSignal(getSuccessSignalId()).mono())
                        .orElse(this::error));
    }

    /**
     * Must be implemented by subclasses.  Execute custom logic and return a result which will
     * determine the outbound signal sent back to the model
     *
     * @return      Result of the execution
     */
    protected abstract Mono<Result<Void, String>> executeLogic();

    /**
     * Result of executing the logic node, which will determine the outbound signal sent back to the model
     */
    protected enum ExecuteLogicResult {
        SUCCESS,
        FAILURE
    }
}
