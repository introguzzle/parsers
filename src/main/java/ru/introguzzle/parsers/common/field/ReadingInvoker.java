package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

public interface ReadingInvoker extends Invoker<Object> {
    @Override
    Object invoke(Field field, Object instance, Object... arguments);

    @Override
    Object invokeStatic(Field field, Object... arguments);
}
