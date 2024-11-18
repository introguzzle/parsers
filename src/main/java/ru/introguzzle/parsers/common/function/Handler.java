package ru.introguzzle.parsers.common.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface Handler extends Consumer<Throwable> {
    /**
     * Handles given {@code throwable}
     *
     * @param throwable throwable to handle
     */
    @Override
    void accept(Throwable throwable);
}
