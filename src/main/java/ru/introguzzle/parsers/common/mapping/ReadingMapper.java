package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapter;

import java.util.Map;
import java.util.Set;

/**
 * A generic interface for mapping objects of type {@code B} to their serialized representations.
 * This mapper facilitates the registration and management of custom {@link TypeAdapter}s,
 * enabling flexible and extensible serialization logic.
 *
 * @param <M> the concrete type of the mapper extending this interface, enabling fluent method chaining
 * @param <B> the base interface type of objects that this mapper can bind to and serialize
 */
public interface ReadingMapper<M extends ReadingMapper<M, B>, B> extends Mapper {
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

    /**
     * Binds the mapper to the specified class type.
     *
     * <p>Binding a class type allows the mapper to recognize and serialize instances of that type.
     * This method may perform initialization or validation necessary for handling the specified type.</p>
     *
     * @param type the {@link Class} object representing the type to bind to
     * @return the current instance of {@code M} to allow method chaining
     * @throws BindException if the binding process fails due to incompatible types or other issues
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NotNull M bindTo(@NotNull Class<? extends B> type) throws BindException;

    /**
     * Unbinds the mapper from the specified class type.
     *
     * <p>Unbinding a class type removes the mapper's ability to recognize and serialize instances of that type.
     * This can be useful for dynamically managing supported types at runtime.</p>
     *
     * @param type the {@link Class} object representing the type to unbind from
     * @return the current instance of {@code M} to allow method chaining
     * @throws BindException if the unbinding process fails, for example, if the type was not previously bound
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NotNull M unbind(@NotNull Class<? extends B> type) throws BindException;

    /**
     * Binds the mapper to multiple class types provided as an array.
     *
     * <p>This default method iterates over the array of class types and binds each one individually.</p>
     *
     * @param types an array of {@link Class} objects representing the types to bind to
     * @return the current instance of {@code ReadingMapper<M, B>} to allow method chaining
     * @throws BindException if the binding process fails for any of the specified types
     * @throws NullPointerException if {@code types} is {@code null} or contains {@code null} elements
     */
    @SuppressWarnings("ALL")
    default @NotNull ReadingMapper<M, B> bindTo(@NotNull Class<? extends B>[] types) throws BindException {
        for (Class<? extends B> type : types) {
            bindTo(type);
        }
        return this;
    }

    /**
     * Binds the mapper to multiple class types provided as a {@link Set}.
     *
     * <p>This default method iterates over the set of class types and binds each one individually.</p>
     *
     * @param types a {@link Set} of {@link Class} objects representing the types to bind to
     * @return the current instance of {@code ReadingMapper<M, B>} to allow method chaining
     * @throws BindException if the binding process fails for any of the specified types
     * @throws NullPointerException if {@code types} is {@code null} or contains {@code null} elements
     */
    @SuppressWarnings("ALL")
    default @NotNull ReadingMapper<M, B> bindTo(@NotNull Set<Class<? extends B>> types) throws BindException {
        for (Class<? extends B> type : types) {
            bindTo(type);
        }
        return this;
    }
}
