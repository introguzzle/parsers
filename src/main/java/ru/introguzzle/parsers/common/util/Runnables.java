package ru.introguzzle.parsers.common.util;

public final class Runnables {
    public static Runnable repeatable(Runnable runnable, int times) {
        return () -> {
            for (int i = 0; i < times; i++) {
                runnable.run();
            }
        };
    }

    /**
     * Composes a few {@code Runnable} objects
     * @param runnable first
     * @param after other
     * @return composed {@code Runnable}
     */
    public static Runnable andThen(Runnable runnable, Runnable... after) {
        return () -> {
            runnable.run();
            for (Runnable r : after) {
                r.run();
            }
        };
    }

    /**
     * Composes two runnables
     * @param runnable second
     * @param before first
     * @return composed {@code Runnable}
     */
    public static Runnable before(Runnable runnable, Runnable before) {
        return () -> {
            before.run();
            runnable.run();
        };
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Runnables() {
        throw Meta.newInstantiationError(Runnables.class);
    }
}
