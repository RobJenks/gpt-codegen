package org.rj.modelgen.llm.state;


public class ModelInterfaceStartSignal<TPayload extends ModelInterfaceDataPayload> extends ModelInterfaceSignal {

    @SuppressWarnings("unchecked")
    public ModelInterfaceStartSignal(String id, TPayload inputPayload) {
        super(id);

        if (inputPayload != null) {
            setPayload(inputPayload.toJsonPayload());
        }
    }

    @Override
    public String getDescription() {
        return "Start a new model interface execution";
    }
}
