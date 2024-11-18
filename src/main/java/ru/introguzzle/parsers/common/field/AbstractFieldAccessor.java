package ru.introguzzle.parsers.common.field;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.util.Streams;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ru.introguzzle.parsers.common.mapping.AccessPolicy.*;

@ExtensionMethod({Streams.class})
@AllArgsConstructor
public abstract class AbstractFieldAccessor<A extends Annotation> implements FieldAccessor {
    public static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private final Map<Integer, Predicate<Field>> FILTERS = Map.of(
            EXCLUDE_TRANSIENT, Fields::isTransient,
            EXCLUDE_FINAL, Fields::isFinal,
            EXCLUDE_STATIC, Fields::isStatic,
            EXCLUDE_VOLATILE, Fields::isVolatile,
            EXCLUDE_PUBLIC, Fields::isPublic,
            EXCLUDE_PROTECTED, Fields::isProtected,
            EXCLUDE_PRIVATE, Fields::isPrivate,
            EXCLUDE_PACKAGE_PRIVATE, Fields::isPackagePrivate
    );

    private final Class<A> annotationType;

    @Override
    public @NotNull List<Field> acquire(@NotNull Class<?> type) {
        return getCache().get(type, this::cache);
    }

    public abstract @NotNull Cache<Class<?>, List<Field>> getCache();

    public abstract List<String> retrieveExcluded(A annotation);
    public abstract int retrieveAccessLevel(A annotation);

    public @NotNull List<Field> cache(@NotNull Class<?> type) {
        A annotation = type.getAnnotation(annotationType);

        List<Field> fields = acquireThroughHierarchy(type);
        Stream<Field> stream = fields.stream();

        int accessPolicy = DEFAULT;

        // Filter out excluded fields, if any to remove
        if (annotation != null) {
            List<String> excluded = retrieveExcluded(annotation);
            accessPolicy = retrieveAccessLevel(annotation);

            stream = stream.reject(field -> excluded.contains(field.getName()));
        }

        for (Map.Entry<Integer, Predicate<Field>> entry : FILTERS.entrySet()) {
            int policy = entry.getKey();
            if ((accessPolicy & policy) == policy) {
                stream = stream.reject(entry.getValue());
            }
        }

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
