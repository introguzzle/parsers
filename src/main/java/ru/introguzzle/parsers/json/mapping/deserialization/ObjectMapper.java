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

    @NotNull Object toObject(@NotNull JSONObject object, @NotNull Type type);
    @NotNull Object[] toArray(@NotNull JSONArray array, @NotNull Type type);
    <E, C extends Collection<E>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Type type, @NotNull Supplier<C> supplier);

    @NotNull InstanceSupplier<JSONObject> getInstanceSupplier();
}
