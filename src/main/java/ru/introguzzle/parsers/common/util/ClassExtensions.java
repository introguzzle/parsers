package ru.introguzzle.parsers.common.util;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Optional;

@UtilityClass
public final class ClassExtensions {
    public static <T> Constructor<T> getDefaultConstructor(Class<T> type) {
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Annotation> Optional<T> getAnnotationAsOptional(Class<?> type, Class<T> annotationType) {
        return Optional.ofNullable(type.getAnnotation(annotationType));
    }
}
