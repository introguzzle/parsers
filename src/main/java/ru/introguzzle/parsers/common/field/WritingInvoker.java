package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

/**
 * Invoker that writes values to fields
 */
public interface WritingInvoker extends Invoker<Void> {
    /**
     * Writes value to instance field
     *
     * @param field instance field of class of {@code instance}
     * @param instance instance
     * @param arguments optional arguments
     * @return always {@code null}
     */
    @Override
    Void invoke(Field field, Object instance, Object... arguments);

    /**
     * Writes value to static field
     *
     * @param field static field of class
     * @param arguments optional arguments
     * @return always {@code null}
     */
    @Override
    Void invokeStatic(Field field, Object... arguments);
}
