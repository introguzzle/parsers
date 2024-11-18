package ru.introguzzle.parsers.common.function;

import java.util.function.Function;

/**
 * Functional interface that defines transforming checked exceptions into unchecked exceptions
 * @param <E> type of resulting unchecked exception
 */
@FunctionalInterface
public interface Transformer<E extends RuntimeException> extends Function<Throwable, E> {
    /**
     * Transforms checked {@code throwable} into unchecked exception
     *
     * @param throwable checked exception
     * @return unchecked exception
     */
    @Override
    E apply(Throwable throwable);

    /**
     * Creates a new transformer that transforms any checked exception into {@code RuntimeException}
     * @return transformer that transforms any unchecked exception into {@code RuntimeException}
     */
    static Transformer<RuntimeException> runtime() {
        return RuntimeException::new;
    }
}
