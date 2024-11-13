package ru.introguzzle.parsers.json.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.field.CachingFieldNameConverter;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;

import java.lang.reflect.Field;

public class JSONFieldNameConverter extends CachingFieldNameConverter<JSONField> {
    private static final Cache<Field, JSONField> CACHE = CacheService.instance().newCache();

    public JSONFieldNameConverter() {
        super(NamingUtilities::toSnakeCase);
    }

    @Override
    public Class<JSONField> getAnnotationType() {
        return JSONField.class;
    }

    @Override
    public String retrieveDefaultValue(JSONField annotation) {
        return annotation.name();
    }

    @Override
    public @NotNull Cache<Field, JSONField> getCache() {
        return CACHE;
    }
}
