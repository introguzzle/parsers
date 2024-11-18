package ru.introguzzle.parsers.common.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Class that provides methods for operating with nullability
 */
public final class Nullability {
    /**
     * Convenient adaptation of {@link java.util.Objects#requireNonNull(Object, String)} method.
     * <p>
     * This method ensures that the provided object reference is not {@code null}.
     * </p>
     *
     * @param object the object reference to check for nullity
     * @param name   the name of the object, used in the exception message if {@code object} is {@code null}
     * @return the non-{@code null} object reference that was validated
     * @param <T>    the type of the object reference
     * @throws NullPointerException if {@code object} is {@code null}
     * @see java.util.Objects#requireNonNull(Object, String)
     */
    public static <T> T requireNonNull(T object, String name) {
        return Objects.requireNonNull(object, name + " cannot be null");
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

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Nullability() {
        throw Meta.newInstantiationError(Nullability.class);
    }
}
