package ru.introguzzle.parsers.common.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * A default implementation of the {@link InstanceSupplier} interface that creates instances
 * using the default (no-argument) constructor of the specified class.
 *
 * @param <T> the type of the source object used during instance acquisition (unused in this implementation)
 */
class DefaultConstructorInstanceSupplier<T> implements InstanceSupplier<T> {

    /**
     * Acquires an instance of the specified type by invoking its default constructor.
     *
     * @param object the source object (not used in this implementation)
     * @param type   the {@code Class} object representing the type to instantiate
     * @param <R>    the type of the object to return
     * @return a new instance of {@code type}
     * @throws MappingException if the default constructor cannot be found or instantiated
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Type type) {
        try {
            return ((Class<R>) type).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MappingException("Cannot find default constructor of class: " + type);
        }
    }
}
