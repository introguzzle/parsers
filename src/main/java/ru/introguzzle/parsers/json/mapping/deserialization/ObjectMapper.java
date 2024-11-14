package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Interface for mapping JSON objects to Java objects.
 *
 * <p>The {@code ObjectMapper} interface defines the contract for converting {@link JSONObject} instances
 * and {@link JSONArray} instances into Java objects of specified types. It leverages a {@link NameConverter}
 * to handle the transformation of field names according to a defined naming strategy (e.g., converting
 * from snake_case to camelCase).</p>
 *
 * <p>Implementations of this interface are responsible for handling the intricacies of
 * JSON deserialization, ensuring type safety, and managing any necessary conversions or
 * custom mappings between JSON structures and Java object fields. This interface extends
 * {@link WritingMapper}, which allows for the inclusion of additional mapping configurations
 * and customizations.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>Conversion of {@link JSONObject} to single Java objects.</li>
 *     <li>Conversion of {@link JSONArray} to arrays of Java objects.</li>
 *     <li>Conversion of {@link JSONArray} to collections of Java objects with customizable collection types.</li>
 *     <li>Support for custom naming strategies via {@link NameConverter}.</li>
 *     <li>Handling of complex JSON structures and nested objects.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Instantiate an ObjectMapper implementation
 * ObjectMapper mapper = new JSONToObjectMapperImpl();
 *
 * // Example JSON data
 * JSONObject jsonObject = new JSONObject();
 * jsonObject.put("id", 1);
 * jsonObject.put("name", "John Doe");
 *
 * // Deserialize JSONObject to a Java object
 * MyClass myObject = mapper.toObject(jsonObject, MyClass.class);
 *
 * // Example JSON array
 * JSONArray jsonArray = new JSONArray();
 * jsonArray.add(jsonObject);
 * jsonArray.add(jsonObject.clone().put("id", 2).put("name", "Jane Smith"));
 *
 * // Deserialize JSONArray to an array of Java objects
 * MyClass[] myObjectsArray = mapper.toArray(jsonArray, MyClass[].class);
 *
 * // Deserialize JSONArray to a collection of Java objects
 * List<MyClass> myObjectsList = mapper.toCollection(jsonArray, MyClass.class, ArrayList::new);
 * }</pre>
 *
 * <p><strong>Exception Handling:</strong></p>
 * <ul>
 *     <li><b>{@link MappingException}</b>: Thrown when an error occurs during the mapping process,
 *     such as type mismatches, missing fields, or issues with the deserialization logic.</li>
 *     <li><b>{@link RuntimeException}</b>: Thrown for unexpected, critical errors that do not fall under
 *     {@code MappingException}. These typically include unchecked exceptions that arise from unforeseen issues.</li>
 * </ul>
 *
 * <p>Implementers should ensure that {@code MappingException} is thrown for known, recoverable issues
 * related to mapping and deserialization, while reserving {@code RuntimeException} for unforeseen, critical errors.</p>
 *
 * @see JSONObject
 * @see JSONArray
 * @see NameConverter
 * @see WritingMapper
 * @see MappingException
 */
public interface ObjectMapper extends WritingMapper<ObjectMapper> {
    NameConverter DEFAULT_CONVERTER = NamingUtilities::toSnakeCase;

    static ObjectMapper newMethodHandleMapper() {
        return newMethodHandleMapper(DEFAULT_CONVERTER);
    }

    static ObjectMapper newMethodHandleMapper(NameConverter nameConverter) {
        return new InvokeObjectMapper(new JSONFieldNameConverter(nameConverter));
    }

    static ObjectMapper newReflectionMapper() {
        return newReflectionMapper(DEFAULT_CONVERTER);
    }

    static ObjectMapper newReflectionMapper(NameConverter nameConverter) {
        return new ReflectionObjectMapper(new JSONFieldNameConverter(nameConverter));
    }

    /**
     * Converts a {@link JSONObject} to an instance of the specified type.
     *
     * <p>This method deserializes the provided {@code JSONObject} into a Java object of the specified
     * {@code type}. It utilizes the configured {@link NameConverter} to map JSON field names to
     * Java object fields according to the defined naming strategy.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * JSONObject jsonObject = new JSONObject();
     * jsonObject.put("id", 1);
     * jsonObject.put("name", "John Doe");
     *
     * MyClass myObject = mapper.toObject(jsonObject, MyClass.class);
     * }</pre>
     *
     * @param object The JSON object to convert.
     * @param type   The class type to convert the JSON object into.
     * @param <T>    The type of the resulting object.
     * @return An instance of type {@code T} representing the converted JSON data.
     * @throws MappingException   If an error occurs during the conversion process, such as type mismatches
     *                            or missing required fields.
     * @throws RuntimeException   If an unexpected error occurs that is not related to the mapping logic.
     */
    <T> @NotNull T toObject(@NotNull JSONObject object, @NotNull Class<T> type);

    /**
     * Converts a {@link JSONArray} to an array of the specified type.
     *
     * <p>This method deserializes the provided {@code JSONArray} into an array of Java objects of the specified
     * {@code type}. It iterates through each element in the JSON array, converting each {@code JSONObject} to
     * an instance of the array's component type using the configured {@link NameConverter}.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * JSONArray jsonArray = new JSONArray();
     * JSONObject jsonObject1 = new JSONObject();
     * jsonObject1.put("id", 1);
     * jsonObject1.put("name", "John Doe");
     * jsonArray.add(jsonObject1);
     *
     * JSONObject jsonObject2 = new JSONObject();
     * jsonObject2.put("id", 2);
     * jsonObject2.put("name", "Jane Smith");
     * jsonArray.add(jsonObject2);
     *
     * MyClass[] myObjectsArray = mapper.toArray(jsonArray, MyClass[].class);
     * }</pre>
     *
     * @param array The JSON array to convert.
     * @param type  The array class type to convert the JSON array into (e.g., {@code MyClass[].class}).
     * @param <T>   The type of the elements in the resulting array.
     * @return An array of type {@code T[]} representing the converted JSON data.
     * @throws MappingException   If an error occurs during the conversion process, such as type mismatches
     *                            or issues with individual array elements.
     * @throws RuntimeException   If an unexpected error occurs that is not related to the mapping logic.
     */
    <T> @NotNull T[] toArray(@NotNull JSONArray array, @NotNull Class<T[]> type);

    /**
     * Converts a {@link JSONArray} to a {@link Collection} of the specified type.
     *
     * <p>This method deserializes the provided {@code JSONArray} into a {@code Collection} of Java objects of the specified
     * {@code type}. The {@code supplier} is used to instantiate the desired {@code Collection} implementation (e.g., {@code ArrayList::new}).
     * Each element in the JSON array is converted to an instance of the collection's generic type using the configured {@link NameConverter}.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * JSONArray jsonArray = new JSONArray();
     * JSONObject jsonObject1 = new JSONObject();
     * jsonObject1.put("id", 1);
     * jsonObject1.put("name", "John Doe");
     * jsonArray.add(jsonObject1);
     *
     * JSONObject jsonObject2 = new JSONObject();
     * jsonObject2.put("id", 2);
     * jsonObject2.put("name", "Jane Smith");
     * jsonArray.add(jsonObject2);
     *
     * List<MyClass> myObjectsList = mapper.toCollection(jsonArray, MyClass.class, ArrayList::new);
     * }</pre>
     *
     * @param array    The JSON array to convert.
     * @param type     The class type of the elements to convert each JSON object into.
     * @param supplier A {@code Supplier} providing instances of the desired {@code Collection} implementation.
     * @param <T>      The type of the elements in the resulting collection.
     * @param <C>      The type of the {@code Collection} to return.
     * @return A {@code Collection} of type {@code C} containing instances of type {@code T} representing the converted JSON data.
     * @throws MappingException   If an error occurs during the conversion process, such as type mismatches
     *                            or issues with individual array elements.
     * @throws RuntimeException   If an unexpected error occurs that is not related to the mapping logic.
     */
    <T, C extends Collection<T>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Class<T> type, @NotNull Supplier<C> supplier);

    @NotNull InstanceSupplier<JSONObject> getInstanceSupplier();
}
