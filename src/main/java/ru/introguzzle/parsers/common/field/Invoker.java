package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

/**
 * The {@code Invoker} interface defines methods for performing operations on fields using reflection.
 * It provides mechanisms to invoke operations on both instance-specific fields and static fields.
 *
 * <p>This interface is typically implemented by classes that handle the dynamic manipulation of field values
 * at runtime. By abstracting the invocation logic, it allows for flexible and reusable code when dealing with
 * various types of fields across different classes.</p>
 *
 * @param <T> the type of the result returned by the invocation methods
 */
public interface Invoker<T> {

    /**
     * Invokes an operation on the specified field of a given instance with the provided arguments.
     *
     * <p>This method is intended for invoking operations on instance fields. The exact nature of the
     * operation (e.g., getting, setting, or manipulating the field value) depends on the implementation
     * of the {@code Invoker} interface.</p>
     *
     * @param field the {@link Field} object representing the field to be operated on
     * @param instance the instance of the object containing the field; may be {@code null} if the field is static
     * @param arguments the arguments to be passed to the operation; can be empty if no arguments are needed
     * @return the result of the invocation, of type {@code T}
     * @throws RuntimeException if any related computing fails
     */
    T invoke(Field field, Object instance, Object... arguments);

    /**
     * Invokes an operation on the specified static field with the provided arguments.
     *
     * <p>This method is intended for invoking operations on static fields. Since static fields are
     * associated with the class rather than any particular instance, the {@code instance} parameter
     * is not required.</p>
     *
     * @param field the {@link Field} object representing the static field to be operated on
     * @param arguments the arguments to be passed to the operation; can be empty if no arguments are needed
     * @return the result of the invocation, of type {@code T}
     * @throws RuntimeException if any related computing fails
     */
    T invokeStatic(Field field, Object... arguments);
}
