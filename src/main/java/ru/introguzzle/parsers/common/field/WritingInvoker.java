package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

public interface WritingInvoker extends Invoker<Void> {
    @Override
    Void invoke(Field field, Object instance, Object... arguments);

    @Override
    Void invokeStatic(Field field, Object... arguments);
}
