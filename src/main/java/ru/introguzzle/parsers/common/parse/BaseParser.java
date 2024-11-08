package ru.introguzzle.parsers.common.parse;

import org.jetbrains.annotations.NotNull;

public interface BaseParser<T> {
    T parse(@NotNull String data);
}
