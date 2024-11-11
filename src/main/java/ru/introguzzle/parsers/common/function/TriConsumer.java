package ru.introguzzle.parsers.common.function;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> before(TriConsumer<? super T, ? super U, ? super V> before) {
        return (t, u, v) -> {
            before.accept(t, u, v);
            accept(t, u, v);
        };
    }

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}