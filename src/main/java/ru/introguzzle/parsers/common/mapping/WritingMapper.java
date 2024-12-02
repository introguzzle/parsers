package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeResolver;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Interface for configuring and managing the serialization mappings of Java objects.
 *
 * <p>The {@code WritingMapper} interface extends the {@link Mapper} interface and provides
 * mechanisms to bind and unbind target types, register custom type adapters, and manage
 * type resolution for serialization purposes. It is designed to support a fluent API,
 * allowing method chaining for configuration.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>Binding and unbinding of target Java types for serialization.</li>
 *     <li>Registration and management of custom {@link TypeAdapter TypeAdapters} for specific types.</li>
 *     <li>Retrieval of a {@link WritingInvoker} to handle field writing operations.</li>
 *     <li>Provision of a forward caller function for type conversions during serialization.</li>
 *     <li>Access to a {@link TypeResolver} for resolving generic types during serialization.</li>
 * </ul>
 *
 * <p><strong>Generic Parameters:</strong></p>
 * <ul>
 *     <li><b>{@code M}</b>: The concrete type that extends {@code WritingMapper}. This enables method
 *     chaining by ensuring that methods return the appropriate subtype.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Instantiate a WritingMapper implementation
 * WritingMapper<MyWritingMapper> mapper = new MyWritingMapperImpl();
 *
 * // Bind a target type for serialization
 * mapper.bindTo(MyClass.class)
 *       .withTypeAdapter(MyClass.class, new MyClassTypeAdapter())
 *       .withTypeAdapters(Map.of(
 *           String.class, new StringTypeAdapter(),
 *           Integer.class, new IntegerTypeAdapter()
 *       ));
 *
 * // Unbind a target type
 * mapper.unbind(MyClass.class);
 *
 * // Serialize an object to JSONObject
 * MyClass myObject = new MyClass(...);
 * JSONObject jsonObject = mapper.toJSONObject(myObject, MappingContext.getDefault());
 * }</pre>
 *
 * @param <M> The concrete type that extends {@code WritingMapper}, enabling fluent method chaining.
 *
 * @see Mapper
 * @see TypeAdapter
 * @see WritingInvoker
 * @see TypeResolver
 * @see MappingException
 */
public interface WritingMapper<M extends WritingMapper<M>> extends Mapper {
    /**
     * Retrieves the {@link WritingInvoker} responsible for handling field writing operations.
     *
     * <p>The {@code WritingInvoker} facilitates the invocation of field setters or direct
     * field access during the serialization process. It abstracts the details of how fields
     * are written to the resulting JSON structure.</p>
     *
     * @return The {@link WritingInvoker} instance used for field writing operations.
     */
    @NotNull
    WritingInvoker getWritingInvoker();

    /**
     * Retrieves the forward caller function used for type conversions during serialization.
     *
     * <p>The {@code BiFunction} returned by this method is used to convert objects from one type
     * to another during the serialization process. It takes an {@code Object} as input and the target
     * {@code Type}, then returns an instance of the target type.</p>
     *
     * @return A {@code BiFunction} that performs type conversions, taking an {@code Object} and a target {@code Type},
     *         and returning an instance of the target type.
     */
    @NotNull
    BiFunction<Object, Type, Object> getForwardCaller();

    /**
     * Finds a registered {@link TypeAdapter} for the specified Java class type.
     *
     * <p>This method searches for a {@link TypeAdapter} that has been registered for the given
     * class type. A {@code TypeAdapter} defines how instances of a particular type are serialized
     * into JSON.</p>
     *
     * @param <T>  The type of the Java class for which the {@code TypeAdapter} is sought.
     * @param type The Java class type for which to find the {@code TypeAdapter}.
     * @return The {@link TypeAdapter} associated with the specified type, or {@code null} if none is found.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    <T> @Nullable TypeAdapter<T> findTypeAdapter(@NotNull Class<T> type);

    /**
     * Registers a {@link TypeAdapter} for the specified Java class type.
     *
     * <p>This method associates a {@link TypeAdapter} with a particular Java class type, enabling
     * customized serialization behavior for that type. If a {@code TypeAdapter} was previously
     * registered for the given type, it will be replaced by the new adapter.</p>
     *
     * @param <T>     The type of the Java class for which the {@code TypeAdapter} is being registered.
     * @param type    The Java class type to associate with the {@code TypeAdapter}.
     * @param adapter The {@link TypeAdapter} to register for the specified type.
     * @return The current instance of {@code M} for method chaining.
     * @throws NullPointerException if {@code type} or {@code adapter} is {@code null}.
     */
    @NotNull
    <T> M withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? extends T> adapter);

    /**
     * Registers multiple {@link TypeAdapter TypeAdapters} for their respective Java class types.
     *
     * <p>This method allows bulk registration of {@code TypeAdapters} by providing a map where
     * each key is a Java class type and the corresponding value is the {@code TypeAdapter}
     * for that type. Existing {@code TypeAdapters} for any of the specified types will be replaced.</p>
     *
     * @param adapters A map containing Java class types as keys and their corresponding {@code TypeAdapter}s as values.
     * @return The current instance of {@code M} for method chaining.
     * @throws NullPointerException if {@code adapters} is {@code null} or contains {@code null} keys or values.
     */
    @NotNull
    M withTypeAdapters(@NotNull Map<Class<?>, @NotNull TypeAdapter<?>> adapters);

    /**
     * Clears all registered {@link TypeAdapter TypeAdapters} from the mapper.
     *
     * <p>After invoking this method, no {@code TypeAdapters} will be registered, and the mapper
     * will revert to its default serialization behavior for all types.</p>
     *
     * @return The current instance of {@code M} for method chaining.
     */
    @NotNull
    M clearTypeAdapters();

    /**
     * Retrieves the {@link TypeResolver} responsible for resolving generic types during serialization.
     *
     * <p>The {@code TypeResolver} is utilized to handle the resolution of generic type parameters,
     * ensuring that the correct type information is available during the serialization process.</p>
     *
     * @return The {@link TypeResolver} instance used for resolving generic types.
     */
    @NotNull
    TypeResolver getTypeResolver();
}
