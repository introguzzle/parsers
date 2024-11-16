package ru.introguzzle.parsers.common.type;

import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.util.Meta;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Optional;

public final class Classes {
    /**
     * Retrieves default constructor without arguments of {@code type}
     *
     * @param type type
     * @return default constructor without arguments
     * @param <T> type
     * @throws MappingException if class of {@code type} has no default constructor without arguments
     */
    public static <T> Constructor<T> retrieveDefaultConstructor(Class<T> type) {
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new MappingException(e);
        }
    }

    /**
     * Retrieves optional annotation of {@code type} by {@code annotationType}.
     * <br>
     * It's the same as {@code Class::getAnnotation}, but this method wraps result in {@code Optional}
     *
     * @param type type
     * @param annotationType annotation type
     * @return optional annotation
     * @param <T> annotation type
     */
    public static <T extends Annotation> Optional<T> retrieveAnnotation(Class<?> type, Class<T> annotationType) {
        return Optional.ofNullable(type.getAnnotation(annotationType));
    }

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private Classes() {
        throw Meta.newInstantiationError(MethodHandles.lookup().lookupClass());
    }
}
