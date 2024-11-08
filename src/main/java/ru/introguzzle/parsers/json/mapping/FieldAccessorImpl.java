package ru.introguzzle.parsers.json.mapping;

import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.field.AbstractFieldAccessor;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldAccessorImpl extends AbstractFieldAccessor<JSONEntity> {
    private static final Cache<Class<?>, List<Field>> CACHE;
    static {
        CACHE = CacheService.getInstance().newCache();
    }

    public FieldAccessorImpl() {
        super(JSONEntity.class);
    }

    @Override
    public Cache<Class<?>, List<Field>> acquireCache() {
        return CACHE;
    }

    @Override
    public List<String> retrieveExcluded(JSONEntity annotation) {
        return Arrays.stream(annotation.excluded()).map(Excluded::value).toList();
    }

    @Override
    public int retrieveAccessLevel(JSONEntity annotation) {
        return annotation.accessLevel();
    }
}
