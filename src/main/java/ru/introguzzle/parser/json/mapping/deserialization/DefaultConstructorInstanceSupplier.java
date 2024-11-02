package ru.introguzzle.parser.json.mapping.deserialization;

import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.MappingInstantiationException;

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
