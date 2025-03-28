package org.rj.modelgen.llm.statemodel.states.common;

import org.rj.modelgen.llm.component.Component;
import org.rj.modelgen.llm.component.ComponentLibrary;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class InsertSyntheticComponents<TComponent extends Component> extends ExecuteLogic {

    private static final Logger LOG = LoggerFactory.getLogger(InsertSyntheticComponents.class);

    public InsertSyntheticComponents() {
        this(InsertSyntheticComponents.class);
    }

    public InsertSyntheticComponents(Class<? extends InsertSyntheticComponents> cls) {
        super(cls);
    }

    @Override
    public String getDescription() {
        return "Insert synthetic components into the model component library";
    }

    @Override
    protected Mono<Result<Void, String>> executeLogic() {
        final var components = getSyntheticComponents();
        if (components == null || components.isEmpty()) {
            LOG.warn("InsertSyntheticComponents: No synthetic components available to insert");
            return Mono.just(Result.Ok());
        }

        addComponentsToModel(getModel(), components);
        return Mono.just(Result.Ok());
    }

    /**
     * Implemented by subclasses. Return the list of synthetic components to be inserted into the model
     */
    protected abstract List<TComponent> getSyntheticComponents();

    /**
     * Implemented by subclasses.  Insert the given set of components into the model
     */
    protected abstract void addComponentsToModel(ModelInterfaceStateMachine model, List<TComponent> components);

}
