package ru.introguzzle.parsers.common.util;

public final class Meta {
    /**
     * Creates new {@code AssertionError} for using it in private constructors
     * with detailed message
     *
     * @param type class
     * @return assertion error
     */
    public static AssertionError newInstantiationError(Class<?> type) {
        return new AssertionError(type + " is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("ALL")
    /**
     * Throws new {@code AssertionError}
     *
     * @param type class
     */
    public static void /** never */ throwInstantiationError(Class<?> type) {
        throw newInstantiationError(type);
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Meta() {
        throwInstantiationError(Meta.class);
    }
}
