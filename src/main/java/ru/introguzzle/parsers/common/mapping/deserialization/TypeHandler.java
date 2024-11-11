package ru.introguzzle.parsers.common.mapping.deserialization;

import java.util.Map;
import java.util.function.Function;

/**
 * Functional interface for handling custom type conversions.
 *
 * <p>{@link TypeHandler} allows customization of how specific types are mapped
 * from JSON objects to Java objects. Implementations of this interface
 * are applied during the conversion process, where each {@link TypeHandler}
 * instance is associated with a target field type T.</p>
 *
 * <p>This interface extends {@link Function} with a more specific purpose,
 * handling type-specific conversions as part of the JSON deserialization
 * process. A {@link TypeHandler} takes an {@link Object} as input and
 * converts it to an instance of field type T.</p>
 *
 * @param <T> the target field type for conversion
 */
public interface TypeHandler<T> extends Function<Object, T> {
    /**
     * Applies the conversion to the provided object.
     *
     * @param object The input object to convert.
     * @return The converted object of type T.
     */
    @Override
    T apply(Object object);

    static <T> TypeHandler<T> of(Function<Object, ? extends T> function) {
        return function::apply;
    }

    static <T> Map.Entry<Class<T>, TypeHandler<T>> newEntry(Class<T> type, Function<Object, ? extends T> function) {
        return Map.entry(type, of(function));
    }
}
