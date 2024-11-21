package ru.introguzzle.parsers.common.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Interface defining methods for handling generic types.
 * <p>
 * This interface provides methods for:
 * <ul>
 *   <li>Resolving field types against the raw class, considering generics.</li>
 *   <li>Converting types to their raw {@link Class} representation.</li>
 *   <li>Retrieving component types for arrays or collections.</li>
 * </ul>
 * <p>
 * Implementations of this interface are used to support working with types that might contain generics,
 * such as collections (e.g., {@link List} or {@link Map}).
 *
 * @see TypeToken
 */
public interface TypeResolver {
    /**
     * Gets the {@link FieldAccessor} used to retrieve fields from classes.
     *
     * @return the {@link FieldAccessor} that provides access to class fields.
     */
    FieldAccessor getFieldAccessor();

    /**
     * Resolves the actual types for fields in a class, considering generics.
     * <p>
     * For example, for a type like {@code Map<String, Integer>}, this method would return
     * a map where:
     * <ul>
     *   <li>The key type would be {@link String},</li>
     *   <li>The value type would be {@link Integer}.</li>
     * </ul>
     *
     * @param rawType the raw (non-generic) class type (e.g., {@link Map}).
     * @param type the type with full information, including generics, obtained from {@link TypeToken}.
     * @return a map where the keys are field names and the values are their resolved types.
     * @see TypeToken
     */
    Map<String, Type> resolveTypes(Class<?> rawType, Type type);

    /**
     * Converts a type into its raw {@link Class} representation.
     * <p>
     * For example, if the input type is {@link List<String>}, the method would return {@code List.class}.
     *
     * @param type the type, which may contain generic parameters.
     * @return the raw class representation of the type (i.e., the class without generics).
     */
    Class<?> getRawType(Type type);

    /**
     * Extracts the component type from the given type.
     * <p>
     * For example, for the type {@link List<String>}, this method would return {@code String.class}.
     * If the type is not an array, this method returns {@code null}.
     *
     * @param type the type from which to extract the component type.
     * @return the component type if the type is an array, otherwise {@code null}.
     */
    Class<?> getComponentType(@NotNull Type type);

    /**
     * Creates a new instance of {@link TypeResolver}.
     *
     * @param fieldAccessor the {@link FieldAccessor} used for retrieving fields from classes.
     * @return a new implementation of {@link TypeResolver}.
     */
    static @NotNull TypeResolver newResolver(@NotNull FieldAccessor fieldAccessor) {
        return new TypeResolverImpl(fieldAccessor);
    }
}
