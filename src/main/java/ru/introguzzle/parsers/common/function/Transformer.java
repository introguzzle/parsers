package ru.introguzzle.parsers.common.function;

import java.util.function.Function;

@FunctionalInterface
public interface Transformer<E extends RuntimeException> extends Function<Throwable, E> {

}
