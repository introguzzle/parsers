package ru.introguzzle.parser.common.utilities;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@UtilityClass
public final class UnsafeUtilities {
    private static Unsafe UNSAFE;

    public static Unsafe acquire() {
        if (UNSAFE != null) {
            return UNSAFE;
        }

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return UNSAFE = (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> type) throws InstantiationException {
        return (T) acquire().allocateInstance(type);
    }
}
