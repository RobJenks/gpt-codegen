package org.rj.modelgen.llm.state;


public class ModelInterfaceStartSignal<TPayload extends ModelInterfaceInputPayload> extends ModelInterfaceSignal {

    @SuppressWarnings("unchecked")
    public ModelInterfaceStartSignal(String id, TPayload inputPayload) {
        super(id);

        if (inputPayload != null) {
            setPayload(inputPayload);
        }
    }

    public <E extends Enum<E>> ModelInterfaceStartSignal(E id, TPayload inputPayload) {
        this(id.toString(), inputPayload);
    }

    @Override
    public String getDescription() {
        return "Start a new model interface execution";
    }
}
