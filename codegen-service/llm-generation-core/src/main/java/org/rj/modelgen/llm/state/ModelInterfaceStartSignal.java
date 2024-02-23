package org.rj.modelgen.llm.state;


public abstract class ModelInterfaceStartSignal<TPayload extends ModelInterfaceDataPayload> extends ModelInterfaceSignal {

    @SuppressWarnings("unchecked")
    public ModelInterfaceStartSignal(Class<? extends ModelInterfaceStartSignal<TPayload>> cls, TPayload inputPayload) {
        super(cls);

        if (inputPayload != null) {
            setPayload(inputPayload.toJsonPayload());
        }
    }

    @Override
    public String getDescription() {
        return "Start a new model interface execution";
    }
}
