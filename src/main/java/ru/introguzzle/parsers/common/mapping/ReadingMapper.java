package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapter;

import java.util.Map;

/**
 * A generic interface for mapping objects of type {@code B} to their serialized representations.
 * This mapper facilitates the registration and management of custom {@link TypeAdapter}s,
 * enabling flexible and extensible serialization logic.
 *
 * @param <M> the concrete type of the mapper extending this interface, enabling fluent method chaining
 */
public interface ReadingMapper<M extends ReadingMapper<M>> extends Mapper {
    /**
     * Retrieves the {@link ReadingInvoker} responsible for reading fields during serialization.
     *
     * @return a non-null {@link ReadingInvoker} instance
     */
    @NotNull
    ReadingInvoker getReadingInvoker();

    /**
     * Registers a custom {@link TypeAdapter} for the specified type.
     *
     * <p>This method allows defining how instances of a particular type should be serialized.
     * Handlers registered using this method take precedence over default handlers.</p>
     *
     * @param <T>          the type for which the handler is being registered
     * @param type         the {@link Class} object representing the type to register the handler for
     * @param typeAdapter  the {@link TypeAdapter} that defines the serialization logic for the specified type
     * @return the current instance of {@code M} to allow method chaining
     * @throws NullPointerException if either {@code type} or {@code typeHandler} is {@code null}
     */
    <T> @NotNull M withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? super T> typeAdapter);

    /**
     * Registers multiple {@link TypeAdapter}s at once.
     *
     * <p>This method allows bulk registration of adapters for various types.
     * Handlers registered using this method take precedence over default adapters.</p>
     *
     * @param adapters a {@link Map} where keys are {@link Class} objects representing the types
     *                     and values are their corresponding {@link TypeAdapter}s
     * @return the current instance of {@code M} to allow method chaining
     * @throws NullPointerException if {@code typeHandlers} is {@code null} or contains {@code null} keys/values
     */
    @NotNull M withTypeAdapters(@NotNull Map<Class<?>, TypeAdapter<?>> adapters);

    /**
     * Clears all registered {@link TypeAdapter}s.
     *
     * <p>This method removes all custom type handlers that have been registered,
     * reverting to the default serialization behavior.</p>
     *
     * @return the current instance of {@code M} to allow method chaining
     */
    @NotNull M clearTypeAdapters();
}
