package ru.introguzzle.parsers.common.type;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public final class Primitives {
    public static final Set<Class<?>> TYPES = Set.of(
            byte.class,    Byte.class,
            short.class,   Short.class,
            int.class,     Integer.class,
            long.class,    Long.class,
            float.class,   Float.class,
            double.class,  Double.class,
            boolean.class, Boolean.class,
            char.class,    Character.class,
            String.class
    );

    public static boolean isPrimitive(Class<?> type) {
        return TYPES.contains(type);
    }
}
