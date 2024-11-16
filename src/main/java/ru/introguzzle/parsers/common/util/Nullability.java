package ru.introguzzle.parsers.common.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Class that provides methods for operating with nullability
 */
public final class Nullability {
    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Nullability() {
        throw Meta.newInstantiationError(Nullability.class);
    }

    /**
     * Selects first non-null object in {@code objects}, if there is any.
     * Otherwise, returns null
     *
     * @param objects objects to select from
     * @return first non-null object. If all {@code objects} are null, null
     */
    public static Object selectAny(Object... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Selects first non-null object in {@code objects}, if there is any.
     * Otherwise, returns null
     *
     * @param objects objects to select from
     * @return first non-null object. If all {@code objects} are null, null
     */
    @SafeVarargs
    public static <T> T selectSameType(T... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
