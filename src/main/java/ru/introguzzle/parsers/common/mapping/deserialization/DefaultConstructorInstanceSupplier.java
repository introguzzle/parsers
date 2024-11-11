package ru.introguzzle.parsers.common.mapping.deserialization;

import ru.introguzzle.parsers.json.mapping.JSONMappingException;
import java.lang.reflect.InvocationTargetException;

public class DefaultConstructorInstanceSupplier<T> implements InstanceSupplier<T> {
    @Override
    public <R> R get(T object, Class<R> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JSONMappingException("Cannot find default constructor of class: " + type.getSimpleName());
        }
    }
}
