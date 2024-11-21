package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeToken;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;

import java.lang.reflect.Type;
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
 * ObjectMapper mapper = ObjectMapper.newReflectionMapper();
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
    /**
     * Default naming converter that transforms field names to snake_case.
     */
    NameConverter DEFAULT_CONVERTER = NamingUtilities::toSnakeCase;

    /**
     * Creates a new {@code ObjectMapper} instance that utilizes method handles for mapping,
     * employing the default naming converter.
     *
     * @return a new {@code ObjectMapper} instance using method handles and the default naming strategy
     */
    static ObjectMapper newMethodHandleMapper() {
        return newMethodHandleMapper(DEFAULT_CONVERTER);
    }

    /**
     * Creates a new {@code ObjectMapper} instance that utilizes method handles for mapping,
     * with a specified naming converter.
     *
     * @param nameConverter the naming strategy to apply to fields
     * @return a new {@code ObjectMapper} instance using method handles and the provided naming strategy
     */
    static ObjectMapper newMethodHandleMapper(NameConverter nameConverter) {
        return new InvokeObjectMapper(new JSONFieldNameConverter(nameConverter));
    }

    /**
     * Creates a new {@code ObjectMapper} instance that utilizes reflection for mapping,
     * employing the default naming converter.
     *
     * @return a new {@code ObjectMapper} instance using reflection and the default naming strategy
     */
    static ObjectMapper newReflectionMapper() {
        return newReflectionMapper(DEFAULT_CONVERTER);
    }

    /**
     * Creates a new {@code ObjectMapper} instance that utilizes reflection for mapping,
     * with a specified naming converter.
     *
     * @param nameConverter the naming strategy to apply to fields
     * @return a new {@code ObjectMapper} instance using reflection and the provided naming strategy
     */
    static ObjectMapper newReflectionMapper(NameConverter nameConverter) {
        return new ReflectionObjectMapper(new JSONFieldNameConverter(nameConverter));
    }

    /**
     * Converts a {@link JSONObject} into an object of the specified {@code Type}.
     *
     * @param object the JSON object to be deserialized
     * @param type   the target type for deserialization (e.g., {@code new TypeToken<Target<Integer>>() {}.getType()})
     * @return the deserialized object
     * @throws MappingException if deserialization fails due to type mismatches, missing fields, or other mapping issues
     */
    @NotNull
    Object toObject(@NotNull JSONObject object, @NotNull Type type);

    /**
     * Converts a {@link JSONArray} into an array of objects of the specified {@code Type}.
     *
     * @param array the JSON array to be deserialized
     * @param type  the target array type for deserialization (e.g., {@code new TypeToken<Target<Integer>>() {}.getType()})
     * @return an array of deserialized objects
     * @see TypeToken
     * @throws MappingException if deserialization fails due to type mismatches or other mapping issues
     */
    @NotNull
    Object[] toArray(@NotNull JSONArray array, @NotNull Type type);

    /**
     * Converts a {@link JSONObject} into object of the specified class.
     *
     * @param object the JSON object to be deserialized
     * @param type   the target class for deserialization
     * @param <T>    the type of the object to be returned
     * @return the deserialized object of type {@code T}
     * @throws MappingException if deserialization fails due to type mismatches, missing fields, or other mapping issues
     */
    @NotNull
    <T> T toObject(@NotNull JSONObject object, @NotNull Class<? extends T> type);

    /**
     * Converts a {@link JSONArray} into an array of objects of the specified array class.
     *
     * @param array the JSON array to be deserialized
     * @param type  the target array class for deserialization (e.g., {@code Target[].class})
     * @param <T>   the type of objects within the array
     * @return an array of deserialized objects of type {@code T}
     * @throws MappingException if deserialization fails due to type mismatches or other mapping issues
     */
    @NotNull
    <T> T[] toArray(@NotNull JSONArray array, @NotNull Class<? extends T[]> type);

    /**
     * Converts a {@link JSONArray} into a Java {@code Collection} of the specified type,
     * using a provided {@link Supplier} to instantiate the collection.
     *
     * @param array    the JSON array to be deserialized
     * @param type     the target element type for deserialization.
     *                 Either it's {@code Class} or {@link Type} obtained from {@link TypeToken}
     * @param supplier a supplier that provides an instance of the desired collection type
     * @param <E>      the type of elements within the collection
     * @param <C>      the type of the collection to be returned
     * @return a collection of deserialized objects of type {@code E}
     * @throws MappingException if deserialization fails due to type mismatches or other mapping issues
     * @apiNote
     * Example of usage:
     * <pre>{@code
     *     // Suppose we have generic type Target<A, B> and we want
     *     // to deserialize array of Target<Integer, String>
     *     JSONArray array = new JSONArray();
     *     array.add({JSON representation of Target<Integer, String> instance})
     *     array.add({JSON representation of Target<Integer, String> type})
     *
     *     ObjectMapper mapper = ...
     *     java.lang.reflect.Type type = new TypeToken<Target<Integer, String>>() {}.getType();
     *     // Safe cast
     *     List<Target<Integer, String>> list = (List<Target<Integer, String>>) mapper.toCollection(array, type, ArrayList::new);
     * }</pre>
     */
    <E, C extends Collection<E>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Type type, @NotNull Supplier<C> supplier);

    /**
     * Converts a {@link JSONArray} into a Java {@code Collection} of the specified type,
     * using a provided {@link Supplier} to instantiate the collection.
     *
     * @param array    the JSON array to be deserialized
     * @param type     the raw target element type for deserialization (e.g., {@code Target.class})
     * @param supplier a supplier that provides an instance of the desired collection type.
     *                 <strong><p>
     *                 It's recommended that supplier returns a new, empty collection instance (e.g., {@code ArrayList::new})
     *                 </p></strong>
     * @param <E>      the type of elements within the collection.
     * @param <C>      the type of the collection to be returned
     * @return a collection of deserialized objects of type {@code E}
     * @throws MappingException if deserialization fails due to type mismatches or other mapping issues
     * @apiNote
     * Example of usage:
     * <pre>{@code
     *     JSONArray array = new JSONArray();
     *     array.add({JSON representation of object of target element type})
     *     array.add({JSON representation of object of target element type})
     *
     *     ObjectMapper mapper = ...
     *     List<Target> list = mapper.toCollection(array, Target.class, ArrayList::new);
     * }</pre>
     */
    <E, C extends Collection<E>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Class<? extends E> type, @NotNull Supplier<C> supplier);

    /**
     * Retrieves the {@link InstanceSupplier} responsible for providing instances from {@link JSONObject}.
     * This can be used to customize how instances are supplied from {@link JSONObject} during deserialization.
     *
     * @return the {@link InstanceSupplier} for {@link JSONObject}
     */
    @NotNull
    InstanceSupplier<JSONObject> getInstanceSupplier();
}
