package ru.introguzzle.parsers.common.field;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public interface GenericTypeAccessor {
    /**
     * Obtains generic types of field
     *
     * @param field field
     * @return immutable list of types of {@code field}
     */
    List<Class<?>> acquire(@NotNull Field field);
}
