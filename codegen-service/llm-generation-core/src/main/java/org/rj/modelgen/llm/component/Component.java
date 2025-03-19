package org.rj.modelgen.llm.component;

// Represents an element of the model component library
public abstract class Component {

    /**
     * Must be implemented by component subclasses.  Serialize component into its default representation
     *
     * @return String serialization of component
     */
    public abstract String defaultSerialize();

}
