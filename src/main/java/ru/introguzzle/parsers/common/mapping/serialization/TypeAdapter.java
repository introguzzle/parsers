package ru.introguzzle.parsers.common.mapping.serialization;

import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Functional interface for handling the serialization of specific types.
 *
 * <p>A {@code TypeHandler} defines how instances of a particular type are converted into their JSON representation.
 * Implementations of this interface should provide the logic for serializing the given type.</p>
 *
 * @param <T> The type that this handler can process.
 *            <br>{@link Boolean}
 *            <br>{@link Number}
 *            <br>{@link JSONArray}
 *            <br>{@link JSONObject}
 *            <br>{@link String}
 */
@FunctionalInterface
public interface TypeAdapter<T> extends Function<T, Object> {
    /**
     * Applies this handler to the given object.
     *
     * @param object The object to be serialized.
     * @return The serialized form of the object, typically a {@code JSONObject}, {@code JSONArray}, or a primitive type.
     */
    @Override
    Object apply(T object);

    static <T> TypeAdapter<T> of(Function<? super T, Object> function) {
        return function::apply;
    }

    static <T> Entry<Class<T>, TypeAdapter<T>> newEntry(Class<T> type, Function<? super T, Object> function) {
        return Map.entry(type, of(function));
    }
}
