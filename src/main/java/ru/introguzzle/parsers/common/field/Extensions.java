package ru.introguzzle.parsers.common.field;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@UtilityClass
public final class Extensions {
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }
}
