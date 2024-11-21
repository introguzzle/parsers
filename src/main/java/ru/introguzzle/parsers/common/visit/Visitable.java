package ru.introguzzle.parsers.common.visit;

import java.util.function.Consumer;

/**
 *
 * @param <T> type of this class
 * @param <V> type of visitor
 */
public interface Visitable<T, V extends Visitor<T>> extends Consumer<V> {
    /**
     * Accepts {@code visitor} that visits this object
     * @param visitor visitor
     */
    @Override
    @SuppressWarnings("unchecked")
    default void accept(V visitor) {
        visitor.visit((T) this);
    }
}
