package ru.introguzzle.parsers.common.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public final class Nullability {
    public static Object selectAny(Object... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @SafeVarargs
    public static <T> T selectSameType(T... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
