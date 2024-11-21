package ru.introguzzle.parsers.common.function;

import java.util.function.Supplier;

public interface ThrowingSupplier<T> {
    T get() throws Throwable;

    default Supplier<T> asSupplier() {
        return asSupplier(Transformer.runtime());
    }

    default Supplier<T> asSupplier(Transformer<? extends RuntimeException> transformer) {
        return () -> {
            try {
                return get();
            } catch (Throwable e) {
                throw transformer.apply(e);
            }
        };
    }
}
