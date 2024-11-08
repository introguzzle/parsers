package ru.introguzzle.parsers.common.visit;

import java.util.function.Consumer;

public interface Visitable<T, V extends Visitor<T>> extends Consumer<V> {
    @Override
    @SuppressWarnings("unchecked")
    default void accept(V visitor) {
        visitor.visit((T) this);
    }
}
