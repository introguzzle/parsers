package ru.introguzzle.parser.common.field;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

@UtilityClass
@SuppressWarnings("unused")
public final class Extensions {
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static Object getValue(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Annotation> Optional<T> getAnnotationAsOptional(Field field, Class<T> annotationType) {
        return Optional.ofNullable(field.getAnnotation(annotationType));
    }
}
