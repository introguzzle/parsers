package ru.introguzzle.parsers.xml.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.AbstractFieldAccessor;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldAccessorImpl extends AbstractFieldAccessor<XMLEntity> {
    private static final Cache<Class<?>, List<Field>> CACHE;
    static {
        CACHE = CACHE_SUPPLIER.newCache();
    }

    public FieldAccessorImpl() {
        super(XMLEntity.class);
    }

    @Override
    public @NotNull Cache<Class<?>, List<Field>> getCache() {
        return CACHE;
    }

    @Override
    public List<String> retrieveExcluded(XMLEntity annotation) {
        return Arrays.stream(annotation.excluded())
                .map(Excluded::value)
                .toList();
    }

    @Override
    public int retrieveAccessLevel(XMLEntity annotation) {
        return annotation.accessLevel();
    }
}
