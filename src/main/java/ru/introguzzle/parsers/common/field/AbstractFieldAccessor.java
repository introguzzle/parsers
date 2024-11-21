package ru.introguzzle.parsers.common.field;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.AccessPolicy;
import ru.introguzzle.parsers.common.mapping.MappingException;
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

/**
 * Implementation of {@link FieldAccessor}
 * Inheritors of this class should only specify how to get excluded fields and access policy
 * from annotation (since annotations don't support inheritance)
 *
 * @param <A> entity-level annotation that contains such settings as field access policy or excluded fields
 */
@ExtensionMethod({Streams.class})
@AllArgsConstructor
public abstract class AbstractFieldAccessor<A extends Annotation> implements FieldAccessor {
    /**
     * Cache producer
     */
    public static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();

    /**
     * JDK loader
     */
    private static final ClassLoader BOOTSTRAP_LOADER;
    static {
        BOOTSTRAP_LOADER = String.class.getClassLoader();
    }

    /**
     * Filters for corresponding flags
     */
    private static final Map<Integer, Predicate<Field>> FILTERS = Map.of(
            EXCLUDE_TRANSIENT, Fields::isTransient,
            EXCLUDE_FINAL, Fields::isFinal,
            EXCLUDE_STATIC, Fields::isStatic,
            EXCLUDE_VOLATILE, Fields::isVolatile,
            EXCLUDE_PUBLIC, Fields::isPublic,
            EXCLUDE_PROTECTED, Fields::isProtected,
            EXCLUDE_PRIVATE, Fields::isPrivate,
            EXCLUDE_PACKAGE_PRIVATE, Fields::isPackagePrivate,
            EXCLUDE_SYNTHETIC, Field::isSynthetic
    );

    /**
     * Class of entity-level annotation
     */
    private final Class<A> annotationType;

    /**
     * {@inheritDoc}
     * @throws MappingException if actual computation throws {@code IllegalAccessException}
     * or {@code type} is JDK class
     */
    @Override
    public @NotNull List<Field> acquire(@NotNull Class<?> type) {
        if (type.getClassLoader() == BOOTSTRAP_LOADER) {
            throw new MappingException(new IllegalAccessException("Cannot access JDK class"));
        }

        return getCache().get(type, this::cache);
    }

    /**
     * Gets cache of fields with keys as classes.
     *
     * @return cache of fields with keys as classes
     */
    public abstract @NotNull Cache<Class<?>, List<Field>> getCache();

    /**
     * Retrieves immutable list of excluded field names from {@code annotation}
     * @param annotation entity-level annotation
     * @return immutable list of excluded field names from {@code annotation}
     */
    public abstract List<String> retrieveExcluded(A annotation);

    /**
     * Retrieves bit flags of access policy from {@code annotation}
     * @param annotation entity-level annotation
     * @return bit flags of access policy from {@code annotation}
     * @see AccessPolicy
     */
    public abstract int retrieveAccessPolicy(A annotation);

    /**
     * Actual computation method
     * @param type target class to retrieve fields from
     * @return immutable list of fields
     */
    private @NotNull List<Field> cache(@NotNull Class<?> type) {
        A annotation = type.getAnnotation(annotationType);

        List<Field> fields = acquireThroughHierarchy(type);
        Stream<Field> stream = fields.stream();

        int accessPolicy = DEFAULT;

        // Filter out excluded fields, if any to remove
        if (annotation != null) {
            List<String> excluded = retrieveExcluded(annotation);
            accessPolicy = retrieveAccessPolicy(annotation);

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
    private List<Field> acquireThroughHierarchy(Class<?> type) {
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
