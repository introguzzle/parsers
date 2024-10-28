package ru.introguzzle.parser.common;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("unused")
public final class Runnables {
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
