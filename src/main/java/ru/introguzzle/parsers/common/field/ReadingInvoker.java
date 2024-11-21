package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

/**
 * Invoker that reads values from fields
 */
public interface ReadingInvoker extends Invoker<Object> {
    /**
     * Reads value from instance field
     *
     * @param field instance field of class of {@code instance}
     * @param instance instance
     * @param arguments optional arguments
     * @return retrieved value of instance {@code field}
     */
    @Override
    Object invoke(Field field, Object instance, Object... arguments);

    /**
     * Reads value from static field
     *
     * @param field static field of class
     * @param arguments optional arguments
     * @return retrieved value of static {@code field}
     */
    @Override
    Object invokeStatic(Field field, Object... arguments);
}
