package ru.introguzzle.jsonparser.convert.primitive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public interface PrimitiveTypeConverter extends BiFunction<String, Class<?>, Object> {
    @Nullable default Object map(@Nullable String data, @NotNull Class<?> type) {
        if (data == null) return null;

        String trimmed = data.trim();
        return apply(trimmed, type);
    }

    @Override
    @Nullable Object apply(@NotNull String data, @NotNull Class<?> type);
}
