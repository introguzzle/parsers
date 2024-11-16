package ru.introguzzle.parsers.common.field;

import ru.introguzzle.parsers.common.mapping.AccessLevel;
import ru.introguzzle.parsers.common.util.Meta;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class that contains convenient methods for using them as references or as lambdas
 */
public final class Fields {
    /**
     * Determines if field is transient
     * @param field field
     * @return {@code true} if field is transient, otherwise {@code false}
     * @see AccessLevel
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    /**
     * Determines if field is volatile
     * @param field field
     * @return {@code true} if field is volatile, otherwise {@code false}
     * @see AccessLevel
     */
    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    /**
     * Determines if field is static
     * @param field field
     * @return {@code true} if field is static, otherwise {@code false}
     * @see AccessLevel
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Fields() {
        throw Meta.newInstantiationError(Fields.class);
    }
}
