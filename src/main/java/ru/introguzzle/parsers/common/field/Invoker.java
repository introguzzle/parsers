package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

public interface Invoker<T> {
    T invoke(Field field, Object instance, Object... arguments);
    T invokeStatic(Field field, Object... arguments);
}
