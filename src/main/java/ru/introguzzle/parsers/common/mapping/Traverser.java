package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface Traverser<R> {
    <T> Optional<T> findMostSpecificMatch(@NotNull Map<? extends R, ? extends T> map, @NotNull R target);
    <T> Optional<T> findMostGeneralMatch(@NotNull Map<? extends R, ? extends T> map, @NotNull R target);
}
