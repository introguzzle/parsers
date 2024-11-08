package ru.introguzzle.parsers.json.mapping.reference;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CircularReferenceStrategy {
    Object handle(@NotNull Object object);
}
