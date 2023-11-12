package org.rj.modelgen.llm.util;

import java.util.function.Consumer;

public class FuncUtil {

    public static <T> T doVoid(T input, Consumer<T> voidMethod) {
        voidMethod.accept(input);
        return input;
    }

}
