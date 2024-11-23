package ru.introguzzle.parsers.common.mapping.deserialization;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Functional interface for handling custom type conversions.
 *
 * <p>{@link TypeAdapter} allows customization of how specific types are mapped
 * from JSON objects to Java objects. Implementations of this interface
 * are applied during the conversion process, where each {@link TypeAdapter}
 * instance is associated with a target field type T.</p>
 *
 * <p>This interface extends {@link Function} with a more specific purpose,
 * handling type-specific conversions as part of the JSON deserialization
 * process. A {@link TypeAdapter} takes an {@link Object} as input and
 * converts it to an instance of field type T.</p>
 *
 * @param <T> the target field type for conversion
 */
public interface TypeAdapter<T> extends BiFunction<Object, Type, T> {
    /**
     * Applies the conversion to the provided source.
     *
     * @param source the input source to convert
     * @param type actual type
     * @return the converted source of type {@code T}
     * @apiNote
     * Brief explanation:
     * <pre>{@code
     * // Suppose we have this class
     * @RequiredArgsConstructor
     * public class Target<A, B extends List<A>, C extends Serializable> {
     *     private final GenericTyped<A, B, C> typedField;
     * }
     *
     * // Then we have JSON representation of instance of this class
     * // Since JSON don't know anything about actual types and during deserialization
     * // back to Target class, solution to this problem would be this interface providing actual information about specific instance
     * // (unless it's raw though)
     *
     * // So when dealing with WritingMapper we can register TypeAdapter for Target class like this:
     * WritingMapper<?> mapper = ...
     * mapper.withTypeAdapter(GenericTyped.class, (source, type) -> {
     *      // source should be JSON object representation of typedField
     *      // (requires casting since it also can be String or JSONArray, depends on ReadingMapper that produced representation)
     *
     *      ParameterizedType pt = (ParameterizedType) type;
     *      Type[] actualTypes = pt.getActualTypeArguments();
     *      Type a = actualTypes[0];
     *      Type b = actualTypes[1];
     *      Type c = actualTypes[2];
     *      // these types can be either Class<?> or again ParameterizedType
     *      // to handle nested structures and nested generic types we can use mapper.getForwardCaller(nestedValue, type)
     *      // ... and finally return appropriate GenericTyped instance or subclass
     * });
     *
     * }</pre>
     */
    @Override
    T apply(Object source, @NotNull Type type);

    /**
     * Creates a {@code TypeAdapter} from a given {@code BiFunction}.
     *
     * @param function the bi-function to use for type conversion
     * @param <T>      the target field type for conversion
     * @return a {@code TypeAdapter} that applies the given function
     * @throws NullPointerException if {@code function} is {@code null}
     */
    static <T> TypeAdapter<T> of(BiFunction<Object, Type, ? extends T> function) {
        return function::apply;
    }

    /**
     * Creates a map entry associating a class type with its corresponding {@code TypeAdapter}.
     *
     * @param type     the class type to associate with the adapter
     * @param function the bi-function defining how to convert objects of the given type
     * @param <T>      the target field type for conversion
     * @return a map entry containing the class type and its associated {@code TypeAdapter}
     * @throws NullPointerException if {@code type} or {@code function} is {@code null}
     */
    static <T> Map.Entry<Class<T>, TypeAdapter<T>> newEntry(Class<T> type, BiFunction<Object, Type, ? extends T> function) {
        return Map.entry(type, of(function));
    }
}
