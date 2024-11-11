package ru.introguzzle.parsers.common.field;

import java.lang.reflect.Field;

public final class ReflectionInvoker {
    private ReflectionInvoker() {}

    public static final class Reading implements ReadingInvoker {
        @Override
        public Object invoke(Field field, Object instance, Object... arguments) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object invokeStatic(Field field, Object... arguments) {
            try {
                return field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class Writing implements WritingInvoker {

        @Override
        public Void invoke(Field field, Object instance, Object... arguments) {
            try {
                field.set(instance, arguments[0]);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        public Void invokeStatic(Field field, Object... arguments) {
            try {
                field.set(null, arguments[0]);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return null;
        }
    }
}
