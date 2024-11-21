package ru.introguzzle.parsers.common.field;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Interface for accessing and retrieving fields from a given class.
 *
 * <p>
 * Implementations of this interface are responsible for providing a list of {@link Field}
 * instances that are relevant for processing, such as serialization or deserialization.
 * </p>
 *
 * <p>
 * <strong>Important:</strong> There is no need to call {@code setAccessible(true)} on the fields
 * returned by the {@link #acquire(Class)} method, as they are already accessible. This design choice
 * ensures that reflection operations remain efficient and do not require additional security permissions.
 * </p>
 *
 * <p>
 * Implementations should consider caching the results of the {@code get} method to optimize performance,
 * especially when dealing with classes that are accessed frequently.
 * </p>
 *
 * @see java.lang.reflect.Field
 */
public interface FieldAccessor {

    /**
     * Retrieves an immutable {@link List} of {@link Field} instances from the specified class that are relevant
     * for processing. The returned fields are already accessible, eliminating the need to
     * modify their accessibility using {@code setAccessible(true)}.
     *
     * <p>
     * This method may filter fields based on certain criteria, such as annotations, access levels,
     * or exclusion rules defined by the implementing class.
     * </p>
     *
     * <p>
     * Implementations are encouraged to cache the results of this method to enhance performance
     * and reduce the overhead of repeated reflection operations.
     * </p>
     *
     * @param type the {@code Class} object from which to retrieve fields
     * @return an immutable {@code List} of {@link Field} instances that are relevant for processing
     * @throws NullPointerException if the {@code type} parameter is {@code null}
     */
    @NotNull List<Field> acquire(@NotNull Class<?> type);
}
