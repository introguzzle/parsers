package ru.introguzzle.parsers.common.field;

import ru.introguzzle.parsers.common.mapping.AccessPolicy;
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
     * @see AccessPolicy
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    /**
     * Determines if field is final
     * @param field field
     * @return {@code true} if field is final, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * Determines if field is static
     * @param field field
     * @return {@code true} if field is static, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * Determines if field is volatile
     * @param field field
     * @return {@code true} if field is volatile, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    /**
     * Determines if field is public
     * @param field field
     * @return {@code true} if field is public, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isPublic(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }

    /**
     * Determines if field is protected
     * @param field field
     * @return {@code true} if field is protected, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isProtected(Field field) {
        return Modifier.isProtected(field.getModifiers());
    }

    /**
     * Determines if field is private
     * @param field field
     * @return {@code true} if field is private, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isPrivate(Field field) {
        return Modifier.isPrivate(field.getModifiers());
    }

    /**
     * Determines if field is package private
     * @param field field
     * @return {@code true} if field is package private, otherwise {@code false}
     * @see AccessPolicy
     */
    public static boolean isPackagePrivate(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers)
                && !Modifier.isPrivate(modifiers);
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Fields() {
        Meta.throwInstantiationError(Fields.class);
    }
}
