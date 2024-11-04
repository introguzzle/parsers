package ru.introguzzle.parser.json.mapping.deserialization;

import ru.introguzzle.parser.common.convert.NameConverter;
import ru.introguzzle.parser.common.field.FieldAccessor;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.FieldNameConverter;
import ru.introguzzle.parser.json.mapping.MappingException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface for mapping JSON objects to Java objects.
 *
 * <p>This interface defines the contract for converting {@link JSONObject} instances
 * into Java objects of a specified type. It leverages a {@link NameConverter} to handle
 * the transformation of field names according to a defined naming strategy (e.g., converting
 * from snake_case to camelCase).</p>
 *
 * <p>Implementations of this interface are responsible for handling the intricacies of
 * JSON deserialization, ensuring type safety, and managing any necessary conversions or
 * custom mappings between JSON structures and Java object fields.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * JSONToObjectMapper mapper = new JSONToObjectMapperImpl();
 * JSONObject jsonObject = ...; // assume this is your JSON data
 * MyClass myObject = mapper.toObject(jsonObject, MyClass.class);
 * }</pre>
 *
 * @see JSONObject
 * @see NameConverter
 */
public interface ObjectMapper {
    FieldNameConverter<Field> getNameConverter();
    BiFunction<Object, Class<?>, Object> getForwardCaller();

    FieldAccessor getFieldAccessor();

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
    interface TypeHandler<T> extends Function<Object, T> {
        /**
         * Applies the conversion to the provided object.
         *
         * @param object The input object to convert.
         * @return The converted object of type T.
         */
        @Override
        T apply(Object object);
    }

    <T> ObjectMapper withTypeHandler(Class<T> type, TypeHandler<? extends T> typeHandler);
    ObjectMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers);
    ObjectMapper clearTypeHandlers();

    <T> TypeHandler<T> getTypeHandler(Class<T> type);

    /**
     * Converts a {@link JSONObject} to an instance of the specified type.
     *
     * @param object The JSON object to convert.
     * @param type   The class type to convert the JSON object into.
     * @param <T>    The type of the resulting object.
     * @return An instance of type T representing the converted JSON data.
     * @throws MappingException If an error occurs during the conversion process.
     */
    <T> T toObject(JSONObject object, Class<T> type);

    <T> T[] toArray(JSONArray array, Class<T[]> type);
    <T, R extends Collection<T>> R toCollection(JSONArray array, Class<T> type, Supplier<R> supplier);
}
