package ru.introguzzle.parsers.json.mapping.deserialization;

import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.json.mapping.MappingException;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
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
    FieldNameConverter<? extends Annotation> getNameConverter();
    BiFunction<Object, Class<?>, Object> getForwardCaller();

    FieldAccessor getFieldAccessor();

    <T> ObjectMapper withTypeHandler(Class<T> type, TypeHandler<? extends T> typeHandler);
    @SuppressWarnings("unused")
    ObjectMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers);

    @SuppressWarnings("unused")
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
