package ru.introguzzle.parsers.common.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.lang.reflect.InvocationTargetException;

public class DefaultConstructorInstanceSupplier<T> implements InstanceSupplier<T> {
    @Override
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Class<R> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MappingException("Cannot find default constructor of class: " + type.getSimpleName());
        }
    }
}
