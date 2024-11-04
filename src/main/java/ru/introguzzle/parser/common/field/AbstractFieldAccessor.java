package ru.introguzzle.parser.common.field;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ru.introguzzle.parser.common.AccessLevel.DEFAULT;
import static ru.introguzzle.parser.common.AccessLevel.EXCLUDE_STATIC;
import static ru.introguzzle.parser.common.AccessLevel.EXCLUDE_TRANSIENT;
import static ru.introguzzle.parser.common.AccessLevel.EXCLUDE_VOLATILE;

@ExtensionMethod({ReflectionUtilities.class, Extensions.class})
@AllArgsConstructor
public abstract class AbstractFieldAccessor<A extends Annotation> implements FieldAccessor {
    private final Class<A> annotationType;

    @Override
    public List<Field> get(Class<?> type) {
        return accessCache().computeIfAbsent(type, this::cache);
    }

    public abstract Map<Class<?>, List<Field>> accessCache();

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

            stream = stream.filter(field -> !excluded.contains(field.getName()));
        }

        if ((accessLevel & EXCLUDE_TRANSIENT) == EXCLUDE_TRANSIENT)
            stream = stream.filter(f -> !f.isTransient());

        if ((accessLevel & EXCLUDE_STATIC) == EXCLUDE_STATIC)
            stream = stream.filter(f -> !f.isStatic());

        if ((accessLevel & EXCLUDE_VOLATILE) == EXCLUDE_VOLATILE)
            stream = stream.filter(f -> !f.isVolatile());

        return stream.toList();
    }
}
