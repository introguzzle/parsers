package ru.introguzzle.parsers.common.field;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.AccessLevel;
import ru.introguzzle.parsers.common.Streams;
import ru.introguzzle.parsers.common.cache.Cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ExtensionMethod({Streams.class})
@AllArgsConstructor
public abstract class AbstractFieldAccessor<A extends Annotation> implements FieldAccessor {
    static final int DEFAULT = AccessLevel.DEFAULT;
    static final int EXCLUDE_TRANSIENT = AccessLevel.EXCLUDE_TRANSIENT;
    static final int EXCLUDE_STATIC = AccessLevel.EXCLUDE_STATIC;
    static final int EXCLUDE_VOLATILE = AccessLevel.EXCLUDE_VOLATILE;

    private final Class<A> annotationType;

    @Override
    public List<Field> acquire(Class<?> type) {
        return getCache().get(type, this::cache);
    }

    public abstract Cache<Class<?>, List<Field>> getCache();

    public abstract List<String> retrieveExcluded(A annotation);
    public abstract int retrieveAccessLevel(A annotation);

    public List<Field> cache(Class<?> type) {
        A annotation = type.getAnnotation(annotationType);

        List<Field> fields = acquireThroughHierarchy(type);
        Stream<Field> stream = fields.stream();

        int accessLevel = DEFAULT;

        // Filter out excluded fields, if any to remove
        if (annotation != null) {
            List<String> excluded = retrieveExcluded(annotation);
            accessLevel = retrieveAccessLevel(annotation);

            stream = stream.reject(field -> excluded.contains(field.getName()));
        }

        if ((accessLevel & EXCLUDE_TRANSIENT) == EXCLUDE_TRANSIENT)
            stream = stream.reject(Extensions::isTransient);

        if ((accessLevel & EXCLUDE_STATIC) == EXCLUDE_STATIC)
            stream = stream.reject(Extensions::isStatic);

        if ((accessLevel & EXCLUDE_VOLATILE) == EXCLUDE_VOLATILE)
            stream = stream.reject(Extensions::isVolatile);

        return stream.toList();
    }

    /**
     * Retrieves all fields of the specified class, including fields from its superclasses,
     * regardless of the access modifier (private, protected, public).
     *
     * @param type the class from which to retrieve all fields
     * @return a list of all fields in the hierarchy of the class
     */
    public List<Field> acquireThroughHierarchy(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                fields.add(field);
            }

            type = type.getSuperclass();
        }

        return fields;
    }
}
