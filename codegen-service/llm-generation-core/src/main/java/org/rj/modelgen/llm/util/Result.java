package org.rj.modelgen.llm.util;

import java.util.Optional;
import java.util.function.Function;

public class Result<T, E> {
    private final T value;
    private final E error;
    private final boolean valid;

    public static <T, E> Result<T, E> Ok(T value) {
        return new Result<>(value, null, true);
    }

    public static <T, E> Result<T, E> Err(E error) {
        return new Result<>(null, error, false);
    }

    public boolean isOk() {
        return valid;
    }

    public boolean isErr() {
        return !isOk();
    }

    public T getValue() {
        if (!valid) throw new IllegalStateException("Result does not contain a valid value");
        return value;
    }

    public Optional<T> getValueIfPresent() {
        if (!valid) return Optional.empty();
        return Optional.ofNullable(value);
    }

    public T orElse(T defaultIfNotPresent) {
        return valid ? value : defaultIfNotPresent;
    }

    public T orElseThrow(Function<E, RuntimeException> onError) {
        if (!valid) throw onError.apply(error);
        return value;
    }

    public E getError() {
        if (valid) throw new IllegalStateException("Result is not an error");
        return error;
    }

    public <U> Result<U, E> map(Function<T, U> f) {
        if (!valid) return Result.Err(error);
        return Result.Ok(f.apply(value));
    }

    private Result(T value, E error, boolean valid) {
        this.value = value;
        this.error = error;
        this.valid = valid;
    }
}
