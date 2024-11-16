package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A utility class that captures and retains generic type information at runtime.
 * <p>
 * The {@code TypeToken} class is used to overcome type erasure in Java, allowing you to obtain full
 * generic type information at runtime. This is particularly useful for serialization and deserialization
 * frameworks that need to work with generic types.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <p>
 * To capture the generic type information, create an anonymous subclass of {@code TypeToken} with the
 * desired type parameter. Here's how you can use it:
 * </p>
 *
 * <pre>{@code
 * // Capturing the generic type List<String>
 * TypeToken<List<String>> typeToken = new TypeToken<List<String>>() {};
 *
 * // Retrieving the Type
 * Type type = typeToken.getType(); // java.util.List<java.lang.String>
 *
 * // Retrieving the raw type
 * Class<?> rawType = typeToken.getRawType(); // interface java.util.List
 * }</pre>
 *
 * <h2>
 * Note: It's essential to use an anonymous subclass with empty braces {@code {}} to capture the
 * generic type information.
 * </h2>
 *
 * @param <T> the generic type parameter to capture
 */
@Getter
public abstract class TypeToken<T> {
    private final Class<? super T> rawType;
    private final Type type;

    /**
     * Constructs a new TypeToken
     * @throws RuntimeException if generic information is missing
     */
    @SuppressWarnings("unchecked")
    public TypeToken() {
        type = getParentTypeParameter();
        rawType = (Class<? super T>) ((ParameterizedType) type).getRawType();
    }

    private Type getParentTypeParameter() {
        Type parent = getClass().getGenericSuperclass();
        if (parent instanceof Class<?>) {
            throw new RuntimeException("Missing type parameter.");
        } else {
            ParameterizedType parameterized = (ParameterizedType) parent;
            return parameterized.getActualTypeArguments()[0];
        }
    }
}
