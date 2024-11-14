package ru.introguzzle.parsers.json.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.ReadingMapper;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandler;
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
 * <p>Example usage:</p>
 * <pre>{@code
 * ObjectToJSONMapper mapper = new ObjectToJSONMapperImpl()
 *         .withTypeHandler(RuntimeException.class, r -> r.getMessage() + "1000")
 *         .withTypeHandler(MappingException.class, m -> m.getMessage() + "3000");
 *
 * MyClass myObject = new MyClass(...);
 * JSONObject jsonObject = mapper.toJSONObject(myObject, MappingContext.getDefault());
 * }</pre>
 *
 * @see JSONObject
 * @see MappingContext
 * @see NameConverter
 */
@SuppressWarnings("unused")
public interface JSONMapper extends ReadingMapper<JSONMapper, Bindable> {
    static JSONMapper newMapper() {
        return newMapper(NamingUtilities::toSnakeCase);
    }

    static JSONMapper newMapper(NameConverter nameConverter) {
        return new JSONMapperImpl(new JSONFieldNameConverter(nameConverter));
    }

    default @NotNull JSONObject toJSONObject(@NotNull Object object) {
        Objects.requireNonNull(object);
        return toJSONObject(object, MappingContext.getDefault());
    }

    /**
     * Converts a Plain Old Java Object (POJO) to a {@link JSONObject}.
     *
     * <p>This method initiates the serialization process, transforming the given Java object into its JSON representation.
     * It utilizes the registered {@link TypeHandler}s and the specified {@link MappingContext} to manage serialization rules and handle special cases.</p>
     *
     * @param object  The POJO to be serialized.
     * @param context The {@link MappingContext} that provides context information for the serialization process, such as handling circular references.
     * @return A {@link JSONObject} representing the serialized form of the given POJO.
     * @throws MappingException If an error occurs during the serialization process.
     */
    @NotNull JSONObject toJSONObject(@NotNull Object object, @NotNull MappingContext context);

    /**
     * Converts a {@link Iterable} or Java array of POJOs to a {@link JSONArray}.
     *
     * @param iterable The {@link Iterable} of POJOs or Java array of POJOs.
     * @param context  The {@link MappingContext} that provides context information for the serialization process, such as handling circular references.
     * @return A {@link JSONArray} representing the serialized form of the given POJOs.
     * @throws MappingException if {@code iterable} is not instance either of {@link Iterable} or Java array.
     */
    @NotNull JSONArray toJSONArray(@NotNull Object iterable, @NotNull MappingContext context);
}
