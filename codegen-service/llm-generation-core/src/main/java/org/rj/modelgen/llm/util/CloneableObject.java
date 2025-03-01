package org.rj.modelgen.llm.util;

public interface CloneableObject extends Cloneable {
    Object clone() throws CloneNotSupportedException;
}
