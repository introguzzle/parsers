package ru.introguzzle.parsers.common.util;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
class EmptyCollectionConstructorRef<T> implements Supplier<T> {
    private final Supplier<T> supplier;

    @Override
    public final T get() {
        T value = supplier.get();
        if (value instanceof Collection<?> collection && !collection.isEmpty()) {
            throw new IllegalArgumentException("Collection is not empty");
        }

        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            throw new IllegalArgumentException("Map is not empty");
        }

        return value;
    }

    static <T> EmptyCollectionConstructorRef<T> of(Supplier<T> supplier) {
        return new EmptyCollectionConstructorRef<>(supplier);
    }
}
