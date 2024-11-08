package ru.introguzzle.parsers.json.mapping.deserialization;

import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.MappingInstantiationException;

import java.lang.reflect.InvocationTargetException;

public class DefaultConstructorInstanceSupplier implements InstanceSupplier {
    @Override
    public <T> T get(JSONObject object, Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MappingInstantiationException("Cannot find default constructor of class: " + type.getSimpleName());
        }
    }
}
