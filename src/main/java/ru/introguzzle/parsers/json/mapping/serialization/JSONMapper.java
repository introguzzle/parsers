package ru.introguzzle.parsers.json.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.json.mapping.MappingException;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

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
public interface JSONMapper {

    @SuppressWarnings("ALL")
    JSONMapper bindTo(Class<? extends Bindable> type) throws BindException;
    JSONMapper unbind(Class<? extends Bindable> type) throws BindException;

    @SuppressWarnings("ALL")
    default JSONMapper bindTo(Class<? extends Bindable>[] types) throws BindException {
        for (var type : types) {
            bindTo(type);
        }

        return this;
    }

    @SuppressWarnings("ALL")
    default JSONMapper bindTo(Set<Class<? extends Bindable>> types) throws BindException {
        for (var type : types) {
            bindTo(type);
        }

        return this;
    }

    FieldAccessor getFieldAccessor();

    /**
     * Retrieves the {@link TypeHandler} associated with the specified type.
     *
     * <p>If a handler for the given type has been registered, it returns that handler. Otherwise,
     * it attempts to find the most specific handler based on the type hierarchy.</p>
     *
     * @param <T>  The type for which the handler is requested.
     * @param type The class of the type to retrieve the handler for.
     * @return The {@link TypeHandler} for the specified type, or {@code null} if no handler is found.
     */
    <T> TypeHandler<T> findTypeHandler(Class<T> type);

    /**
     * Registers a custom {@link TypeHandler} for the specified type.
     *
     * <p>This method allows defining how instances of a particular type should be serialized into JSON.
     * Handlers registered using this method take precedence over default handlers.</p>
     *
     * @param <T>         The type for which the handler is being registered.
     * @param type        The class of the type to register the handler for.
     * @param typeHandler The {@link TypeHandler} that defines the serialization logic for the specified type.
     * @return The current instance of {@code ObjectToJSONMapper} to allow method chaining.
     */
    <T> JSONMapper withTypeHandler(Class<T> type, TypeHandler<? super T> typeHandler);

    /**
     * Registers multiple {@link TypeHandler}s at once.
     *
     * <p>This method allows you to bulk register handlers for various types. Handlers registered using this
     * method take precedence over default handlers.</p>
     *
     * @param typeHandlers A {@code Map} where keys are classes representing the types and values are their corresponding {@link TypeHandler}s.
     * @return The current instance of {@code ObjectToJSONMapper} to allow method chaining.
     */
    JSONMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers);

    /**
     * Clears all registered {@link TypeHandler}s.
     *
     * <p>This method removes all custom type handlers that have been registered, reverting to the default serialization behavior.</p>
     *
     * @return The current instance of {@code ObjectToJSONMapper} to allow method chaining.
     */
    JSONMapper clearTypeHandlers();

    default JSONObject toJSONObject(@Nullable Object object) {
        if (object == null) return null;

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
    JSONObject toJSONObject(@NotNull Object object, MappingContext context);

    FieldNameConverter<? extends Annotation> getNameConverter();

    /**
     * Converts a {@link Iterable} or Java array of POJOs to a {@link JSONArray}.
     *
     *
     * @param iterable The {@link Iterable} of POJOs or Java array of POJOs.
     * @param context The {@link MappingContext} that provides context information for the serialization process, such as handling circular references.
     * @return A {@link JSONArray} representing the serialized form of the given POJOs.
     * @throws MappingException If
     * <br>
     * Or if {@code iterable} is not instance either of {@link Iterable} or Java array.
     */
    JSONArray toJSONArray(@NotNull Object iterable, MappingContext context);
}
