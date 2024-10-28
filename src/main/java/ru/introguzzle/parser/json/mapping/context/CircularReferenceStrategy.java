package ru.introguzzle.parser.json.mapping.context;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CircularReferenceStrategy {
    Object handle(@NotNull Object object);
}
