package ru.introguzzle.parsers.common.util;

import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
@SuppressWarnings("unused")
public final class Runnables {
    public static Runnable fromFunction(Function<Object, ?> function, Object value) {
        return () -> function.apply(value);
    }

    public static Runnable andThen(Runnable runnable, Runnable... after) {
        return () -> {
            runnable.run();
            for (Runnable r : after) {
                r.run();
            }
        };
    }

    public static Runnable before(Runnable runnable, Runnable... before) {
        return () -> {
            for (Runnable r : before) {
                r.run();
            }

            runnable.run();
        };
    }
}
