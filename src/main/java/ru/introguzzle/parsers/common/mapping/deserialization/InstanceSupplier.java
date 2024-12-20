package ru.introguzzle.parsers.common.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.WritingMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A functional interface responsible for supplying instances of objects during the deserialization process.
 * <p>
 * The {@code InstanceSupplier} interface abstracts the creation of object instances, allowing for flexible
 * instantiation strategies. This can be particularly useful when deserializing XML or JSON data into Java objects,
 * where the instantiation logic may vary based on specific requirements or configurations.
 *
 * <p>Implementations of this interface can define custom logic for object creation, such as using reflection,
 * dependency injection frameworks, or custom constructors. By decoupling the instantiation logic from the
 * deserialization process, {@code InstanceSupplier} promotes cleaner and more maintainable code.</p>
 *
 * <p>Example Usage:</p>
 * <pre>{@code
 * // Implementation using reflection
 * public class ReflectionInstanceSupplier implements InstanceSupplier<Object> {
 *     @Override
 *     public <R> R acquire(Object object, Class<R> type) {
 *         try {
 *             return type.getDeclaredConstructor().newInstance();
 *         } catch (Exception e) {
 *             throw new RuntimeException("Failed to create instance of " + type.getName(), e);
 *         }
 *     }
 * }
 *
 * // Using the InstanceSupplier in a deserialization context
 * InstanceSupplier<Object> supplier = new ReflectionInstanceSupplier();
 * ObjectMapper mapper = new XMLObjectMapperImpl(supplier, new DefaultInflector());
 *
 * XMLDocument xmlDocument = ... // Assume this is provided
 * Company company = mapper.toObject(xmlDocument, Company.class);
 * }</pre>
 *
 * @param <T> the type parameter representing the context or source object used during instance acquisition
 *
 * @see ru.introguzzle.parsers.common.mapping.WritingMapper
 */
@FunctionalInterface
public interface InstanceSupplier<T> {

    /**
     * Acquires an instance of the specified type based on the provided context object.
     *
     * <p>This method is invoked during the deserialization process to create instances of target classes.
     * The implementation can utilize the {@code object} parameter to influence the instantiation logic,
     * such as selecting specific constructors, applying dependency injection, or applying custom initialization.</p>
     *
     * <p>For example, when deserializing an XML element into a Java object, the {@code object} is
     * an {@code XMLDocument} instance containing the data to be mapped to the object's fields.</p>
     *
     * <p><strong>Exception Handling:</strong></p>
     * <ul>
     *     <li><b>{@link MappingException}</b>: Thrown when there is an issue with the instantiation logic,
     *     such as mismatched constructor arguments or reflection failures. For example, if the number of
     *     constructor arguments does not align with the expected parameters, a {@code MappingException}
     *     is thrown to indicate this discrepancy.</li>
     *     <li><b>{@link RuntimeException}</b></b>: Thrown for unexpected errors that do not fall under
     *     {@code MappingException}. This could include unchecked exceptions that occur during the instantiation
     *     process, such as illegal access or invocation target exceptions that are not specifically handled.</li>
     * </ul>
     *
     * <p>Implementers should ensure that {@code MappingException} is thrown for known, recoverable issues
     * related to mapping and instantiation, while reserving {@code RuntimeException} for unforeseen, critical errors.</p>
     *
     * @param object the context or source object that provides additional information for instance creation
     *               (e.g., {@code XMLDocument} containing data to be deserialized)
     * @param type   the {@link Class} object representing the type of the instance to be created
     * @return an instance of type {@code R} as specified by the {@code type} parameter
     *
     * @throws MappingException    if the instance cannot be created due to an error in the instantiation logic,
     *                              such as mismatched constructor arguments or reflection failures
     * @throws RuntimeException    if other unexpected errors occur during the instantiation process
     * @throws NullPointerException if {@code object} is null
     *
     * @see CachingAnnotationInstanceSupplier
     */
    @NotNull <R> R acquire(@NotNull T object, @NotNull Type type);

    /**
     * Convenient method for checking arguments for {@code acquire} method
     * @param object object
     * @param type class
     * @return {@code} object
     */
    default @NotNull T requireNonNull(T object, Type type) {
        Objects.requireNonNull(type, "Type must not be null");
        return Objects.requireNonNull(object, "Object to deserialize must be not null");
    }

    static <T> InstanceSupplier<T> getDefaultConstructorSupplier() {
        return new DefaultConstructorInstanceSupplier<>();
    }

    static <T, E extends Annotation, F extends Annotation>
    InstanceSupplier<T> getReflectionSupplier(WritingMapper<?> mapper,
                                              AnnotationData<E, F> annotationData,
                                              Function<E, ConstructorArgument[]> constructorRetriever,
                                              BiFunction<T, String, Object> valueRetriever) {
        return new ReflectionAnnotationInstanceSupplier<>(mapper, annotationData) {

            @Override
            public ConstructorArgument[] retrieveConstructorArguments(E annotation) {
                return constructorRetriever.apply(annotation);
            }

            @Override
            public Object retrieveValue(T object, String name) {
                return valueRetriever.apply(object, name);
            }
        };
    }

    static <T, E extends Annotation, F extends Annotation>
    InstanceSupplier<T> getMethodHandleSupplier(WritingMapper<?> mapper,
                                                AnnotationData<E, F> annotationData,
                                                Function<E, ConstructorArgument[]> constructorRetriever,
                                                BiFunction<T, String, Object> valueRetriever,
                                                Cache<Class<?>, E> cache) {
        return new CachingAnnotationInstanceSupplier<>(mapper, annotationData) {
            @Override
            public ConstructorArgument[] retrieveConstructorArguments(E annotation) {
                return constructorRetriever.apply(annotation);
            }

            @Override
            public Object retrieveValue(T object, String name) {
                return valueRetriever.apply(object, name);
            }

            @Override
            public Cache<Class<?>, E> getAnnotationCache() {
                return cache;
            }
        };
    }
}
