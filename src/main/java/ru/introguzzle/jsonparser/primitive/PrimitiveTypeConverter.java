package ru.introguzzle.jsonparser.primitive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

public interface PrimitiveTypeConverter extends Function<String, Object> {
    @Nullable default Object map(@Nullable String data) {
        if (data == null) return null;

        String trimmed = data.trim();
        return apply(trimmed);
    }

    @Override
    @Nullable Object apply(@NotNull String data);
}
