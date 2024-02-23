package org.rj.modelgen.llm.state;

public enum ModelInterfaceStandardPayloadData {
    SessionId("sessionId", String.class),
    Request("request", String.class),
    LLM("llm", String.class),
    Temperature("temperature", Float.class);


    private final String key;
    private final Class<?> dataClass;

    ModelInterfaceStandardPayloadData(String key, Class<?> dataClass) {
        this.key = key;
        this.dataClass = dataClass;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }
}
