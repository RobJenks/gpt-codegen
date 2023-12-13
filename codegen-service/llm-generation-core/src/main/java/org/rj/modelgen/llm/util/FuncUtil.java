package org.rj.modelgen.llm.util;

import org.jooq.lambda.tuple.Tuple0;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class FuncUtil {

    public static class None extends Tuple0 {
        public static None create() { return new None(); }
        public static Mono<None> mono() { return Mono.just(create()); }
    }

    public static <T> T doVoid(T input, Consumer<T> voidMethod) {
        voidMethod.accept(input);
        return input;
    }

}
