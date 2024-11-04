package ru.introguzzle.parser.json.mapping;

import ru.introguzzle.parser.common.field.AbstractFieldAccessor;
import ru.introguzzle.parser.json.entity.annotation.JSONEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FieldAccessorImpl extends AbstractFieldAccessor<JSONEntity> {
    private static final Map<Class<?>, List<Field>> CACHE = new ConcurrentHashMap<>();

    public FieldAccessorImpl() {
        super(JSONEntity.class);
    }

    @Override
    public Map<Class<?>, List<Field>> accessCache() {
        return CACHE;
    }

    @Override
    public List<String> retrieveExcluded(JSONEntity annotation) {
        return Arrays.asList(annotation.excluded());
    }

    @Override
    public int retrieveAccessLevel(JSONEntity annotation) {
        return annotation.accessLevel();
    }
}
