package ru.introguzzle.parsers.common.type;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.util.Meta;

import java.lang.invoke.MethodHandles;
import java.util.Set;

public final class Primitives {
    /**
     * Set of types that can be viewed as "primitives". Includes {@code String}
     */
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

    /**
     * Determines if given {@code type} is primitive according to {@linkplain #TYPES}
     * @param type type to test its primitiveness
     * @return {@code true} if {@code type} is primitive, otherwise {@code false}
     */
    public static boolean isPrimitive(Class<?> type) {
        // Note: Class has no overridden equals method,
        // so Set uses default Object reference equality test
        return TYPES.contains(type);
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Primitives() {
        throw Meta.newInstantiationError(MethodHandles.lookup().lookupClass());
    }
}
