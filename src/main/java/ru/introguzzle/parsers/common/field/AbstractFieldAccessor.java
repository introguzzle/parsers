package ru.introguzzle.parsers.common.field;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.AccessLevel;
import ru.introguzzle.parsers.common.Streams;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.utility.ReflectionUtilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

@ExtensionMethod({ReflectionUtilities.class, Streams.class})
@AllArgsConstructor
public abstract class AbstractFieldAccessor<A extends Annotation> implements FieldAccessor {
    static final int DEFAULT = AccessLevel.DEFAULT;
    static final int EXCLUDE_TRANSIENT = AccessLevel.EXCLUDE_TRANSIENT;
    static final int EXCLUDE_STATIC = AccessLevel.EXCLUDE_STATIC;
    static final int EXCLUDE_VOLATILE = AccessLevel.EXCLUDE_VOLATILE;

    private final Class<A> annotationType;

    @Override
    public List<Field> acquire(Class<?> type) {
        return acquireCache().get(type, this::cache);
    }

    public abstract Cache<Class<?>, List<Field>> acquireCache();

    public abstract List<String> retrieveExcluded(A annotation);
    public abstract int retrieveAccessLevel(A annotation);

    public List<Field> cache(Class<?> type) {
        A annotation = type.getAnnotation(annotationType);

        List<Field> fields = type.getAllFields();
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
}
