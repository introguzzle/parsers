package ru.introguzzle.parsers.common.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface Handler extends Consumer<Throwable> {

}
