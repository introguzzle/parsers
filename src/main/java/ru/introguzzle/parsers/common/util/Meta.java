package ru.introguzzle.parsers.common.util;

import java.lang.invoke.MethodHandles;

public final class Meta {

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Meta() {
        throw newInstantiationError(Meta.class);
    }

    /**
     * Creates new {@code AssertionError} for using it in private constructors
     * with detailed message
     *
     * @param type class
     * @return assertion error
     */
    public static AssertionError newInstantiationError(Class<?> type) {
        System.out.println(MethodHandles.lookup().lookupClass());
        return new AssertionError(type + " is a utility class and cannot be instantiated");
    }
}
