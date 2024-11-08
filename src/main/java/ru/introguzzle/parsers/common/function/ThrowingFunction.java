package ru.introguzzle.parsers.common.function;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Throwable;

    default Function<T, R> toFunction() {
        return toFunction(() -> (R) null);
    }

    default Function<T, R> toFunction(R defaultValue) {
        return toFunction(() -> defaultValue);
    }

    default Function<T, R> toFunction(Transformer<? extends RuntimeException> transformer) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable e) {
                throw transformer.apply(e);
            }
        };
    }

    default Function<T, R> toFunction(Supplier<? extends R> supplier) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable e) {
                return supplier.get();
            }
        };
    }

    default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) {
        return v -> apply(Objects.requireNonNull(before).apply(v));
    }

    default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after) {
        return t -> Objects.requireNonNull(after).apply(apply(t));
    }

    static <T> ThrowingFunction<T, T> identity() {
        return t -> t;
    }
}
