package ru.introguzzle.parsers.json.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.field.AbstractFieldAccessor;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class JSONFieldAccessor extends AbstractFieldAccessor<JSONEntity, JSONField> {
    private static final Cache<Class<?>, List<Field>> CACHE;
    static {
        CACHE = CACHE_SUPPLIER.newCache();
    }

    public JSONFieldAccessor() {
        super(AnnotationData.JSON);
    }

    @Override
    public @NotNull Cache<Class<?>, List<Field>> getCache() {
        return CACHE;
    }

    @Override
    public List<String> retrieveExcluded(JSONEntity annotation) {
        return Arrays.stream(annotation.excluded()).map(Excluded::value).toList();
    }

    @Override
    public int retrieveAccessPolicy(JSONEntity annotation) {
        return annotation.accessPolicy();
    }

    @Override
    public boolean retrieveExcludeFlag(JSONField annotation) {
        return annotation.exclude();
    }
}
