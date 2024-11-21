package ru.introguzzle.parsers.json.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.ReadingMapper;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapter;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.util.Objects;

/**
 * Interface for mapping Plain Old Java Objects (POJOs) to {@link JSONObject} instances.
 *
 * <p>This interface defines the contract for converting Java objects into their JSON representation.
 * It provides mechanisms to register custom type handlers that dictate how specific types are serialized.
 * Additionally, it supports configuring naming conventions for JSON field names through a {@link NameConverter}.</p>
 *
 * <p>Implementations of this interface are responsible for handling the serialization process,
 * ensuring that Java objects are accurately and efficiently transformed into JSON format.
 * This includes managing primitive types, complex objects, collections, and handling special cases
 * such as circular references.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>Conversion of Java objects to {@link JSONObject}.</li>
 *     <li>Conversion of Java arrays and collections to {@link JSONArray}.</li>
 *     <li>Support for custom type handlers via {@link TypeAdapter} registration.</li>
 *     <li>Configurable naming strategies for JSON field names using {@link NameConverter}.</li>
 *     <li>Handling of complex and nested Java objects during serialization.</li>
 *     <li>Management of serialization context, including circular reference handling.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Instantiate a JSONMapper implementation with default naming strategy
 * JSONMapper mapper = JSONMapper.newMapper();
 *
 * // Example POJO
 * MyClass myObject = new MyClass(1337, "Example");
 *
 * // Serialize POJO to JSONObject
 * JSONObject jsonObject = mapper.toJSONObject(myObject, MappingContext.getDefault());
 * System.out.println(jsonObject.toJSONString());
 *
 * // Example list of POJOs
 * List<MyClass> list = Arrays.asList(
 *     new MyClass(1, "First"),
 *     new MyClass(2, "Second")
 * );
 *
 * // Serialize list to JSONArray
 * JSONArray jsonArray = mapper.toJSONArray(list, MappingContext.getDefault());
 * System.out.println(jsonArray.toJSONString());
 * }</pre>
 *
 * <p><strong>Exception Handling:</strong></p>
 * <ul>
 *     <li><b>{@link MappingException}</b>: Thrown when an error occurs during the serialization process,
 *     such as type mismatches, inaccessible fields, or issues with custom type handlers.</li>
 *     <li><b>{@link RuntimeException}</b>: Thrown for unexpected, critical errors that do not fall under
 *     {@code MappingException}. These typically include unchecked exceptions that arise from unforeseen issues.</li>
 * </ul>
 *
 * <p>Implementers should ensure that {@code MappingException} is thrown for known, recoverable issues
 * related to serialization, while reserving {@code RuntimeException} for unforeseen, critical errors.</p>
 *
 * @see JSONObject
 * @see JSONArray
 * @see NameConverter
 * @see MappingContext
 * @see TypeAdapter
 * @see MappingException
 */
@SuppressWarnings("unused")
public interface JSONMapper extends ReadingMapper<JSONMapper, Bindable> {
    /**
     * Default naming converter that transforms field names to snake_case.
     */
    NameConverter DEFAULT_CONVERTER = NamingUtilities::toSnakeCase;

    /**
     * Creates a new {@code JSONMapper} instance using the default naming strategy.
     *
     * @return a new {@code JSONMapper} instance with the default naming converter
     */
    static JSONMapper newMapper() {
        return newMapper(DEFAULT_CONVERTER);
    }

    /**
     * Creates a new {@code JSONMapper} instance with a specified naming converter.
     *
     * @param nameConverter the naming strategy to apply to fields
     * @return a new {@code JSONMapper} instance configured with the provided naming converter
     */
    static JSONMapper newMapper(NameConverter nameConverter) {
        return new JSONMapperImpl(new JSONFieldNameConverter(nameConverter));
    }

    /**
     * Converts a Plain Old Java Object (POJO) to a {@link JSONObject} using the default {@link MappingContext}.
     *
     * <p>This method initiates the serialization process, transforming the given Java object into its JSON representation
     * using the default mapping context, which may include default settings for handling circular references and other
     * serialization behaviors.</p>
     *
     * @param object the POJO to be serialized
     * @return a {@link JSONObject} representing the serialized form of the given POJO
     * @throws MappingException if an error occurs during serialization
     * @throws NullPointerException if {@code object} is null
     */
    default @NotNull JSONObject toJSONObject(@NotNull Object object) {
        Objects.requireNonNull(object, "The object to serialize must not be null.");
        return toJSONObject(object, MappingContext.getDefault());
    }

    /**
     * Converts a Plain Old Java Object (POJO) to a {@link JSONObject}.
     *
     * <p>This method initiates the serialization process, transforming the given Java object into its JSON representation.
     * It utilizes the registered {@link TypeAdapter}s and the specified {@link MappingContext} to manage serialization rules
     * and handle special cases such as circular references.</p>
     *
     * @param object  the POJO to be serialized
     * @param context the {@link MappingContext} that provides context information for the serialization process,
     *                such as handling circular references and custom serialization behaviors
     * @return a {@link JSONObject} representing the serialized form of the given POJO
     * @throws MappingException if serialization fails due to type mismatches, inaccessible fields, or other mapping issues
     * @throws NullPointerException if {@code object} or {@code context} is null
     */
    @NotNull
    JSONObject toJSONObject(@NotNull Object object, @NotNull MappingContext context);

    /**
     * Converts an array of objects to a {@link JSONArray}.
     *
     * <p>This method serializes each element of the provided array into its JSON representation and aggregates them into a
     * {@link JSONArray}. The specified {@link MappingContext} governs the serialization behavior, including handling of
     * nested objects and circular references.</p>
     *
     * @param array   the array of objects to be serialized
     * @param context the {@link MappingContext} that provides context information for the serialization process,
     *                such as handling circular references and custom serialization behaviors
     * @return a {@link JSONArray} representing the serialized form of the given array of objects
     * @throws MappingException if serialization fails due to type mismatches, inaccessible fields, or other mapping issues
     * @throws NullPointerException if {@code array} or {@code context} is null
     */
    @NotNull
    JSONArray toJSONArray(@NotNull Object[] array, @NotNull MappingContext context);

    /**
     * Converts an {@link Iterable} of objects to a {@link JSONArray}.
     *
     * <p>This method serializes each element of the provided {@link Iterable} into its JSON representation and aggregates them into a
     * {@link JSONArray}. The specified {@link MappingContext} governs the serialization behavior, including handling of
     * nested objects and circular references.</p>
     *
     * @param iterable the {@link Iterable} of objects to be serialized
     * @param context  the {@link MappingContext} that provides context information for the serialization process,
     *                 such as handling circular references and custom serialization behaviors
     * @return a {@link JSONArray} representing the serialized form of the given {@link Iterable} of objects
     * @throws MappingException if serialization fails due to type mismatches, inaccessible fields, or other mapping issues
     * @throws NullPointerException if {@code iterable} or {@code context} is null
     */
    @NotNull
    JSONArray toJSONArray(@NotNull Iterable<?> iterable, @NotNull MappingContext context);
}
